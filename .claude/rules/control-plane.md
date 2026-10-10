---
paths:
  - "control-plane/**"
---

# Control plane conventions

Security and API design are built in, not bolted on (OWASP API Top 10 in mind). Examples below are
excerpts from the codebase; the named file is the reference to copy from.

## Layers and API

- **Layers:** controller (HTTP only) -> service (state rules) -> repository (SQL only). Errors go
  through the one `@RestControllerAdvice` (`GlobalExceptionHandler`). Controllers map service
  results to status codes and nothing else (`WorkerController`):

  ```java
  return runService.claimNext(workerId)
          .map((run) -> ResponseEntity.ok(RunResponse.from(run)))
          .orElseGet(() -> ResponseEntity.noContent().build());
  ```

- **Versioned paths** under `/api/v1`, split by caller: `/runs` for clients
  (`RunController`), `/workers/{workerId}/...` for workers (`WorkerController`), so authorization
  can attach per prefix later.
- **Explicit request/response records per endpoint** in `run/dto`. Never bind a body to a domain
  object or a `Map`. Responses convert with a static `from(...)` (`RunResponse.from(run)`).

  ```java
  public record RunCompletionRequest(@NotNull CompletionStatus status) {
  }
  ```

- **Validate at the boundary** with Bean Validation: lengths, patterns, allowlists. An enum is the
  allowlist: `CompletionStatus` accepts only `succeeded` / `failed`. Bodies use `@RequestBody
  @Valid`; path variables carry their own constraint, like the composed `@WorkerId`
  (`run/validation/WorkerId.java`: `@Pattern` + `@Size(max = 64)`):

  ```java
  public ResponseEntity<RunResponse> completeRun(
          @PathVariable @WorkerId String workerId,
          @PathVariable UUID runId,
          @RequestBody @Valid RunCompletionRequest request) {
  ```

- **Problem Details (RFC 9457) for every error.** Each domain exception gets one handler with a
  deliberate status (`RunNotFoundException` -> `404`, `RunNotHeldException` -> `409`). Validation
  failures add an `errors` list of `FieldViolation`. Anything else is a generic `500` that logs the
  exception and returns no detail:

  ```java
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(final Exception exception) {
      log.error("Unhandled exception", exception);

      return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
              "An error occurred");
  }
  ```

  Never return stack traces, SQL, or exception text from outside our own exceptions.
- The claim is `POST`, never `PUT`: it is not idempotent.
- **Log ids, not payloads.**

## SQL and state

- **SQL lives in text blocks in the repository**, columns from the `RUN_COLUMNS` constant, values
  as named `:params`. No request value is ever concatenated into SQL; `.formatted(RUN_COLUMNS)`
  is fine because it is our own fixed fragment (`RunRepository.findById`):

  ```java
  var sql = """
          SELECT %s
          FROM runs
          WHERE id = :id
          """.formatted(RUN_COLUMNS);

  return client.sql(sql)
          .param("id", runUuid)
          .query(Run.class)
          .optional();
  ```

- **Ownership is checked inside the `UPDATE`'s `WHERE`**, never read-then-write. Zero rows changed
  means `409` (`RunRepository.updateStage`, `RunRepository.complete`):

  ```sql
  WHERE id = :runId
      AND worker_id = :workerId
      AND status = 'RUNNING'
  ```

- 404 vs 409 after a zero-row update is decided by `existsById` in `RunService.notHeldOrNotFound`,
  on the error path only.
- The claim picks a row with `FOR UPDATE SKIP LOCKED` inside the `UPDATE`'s subquery, so
  concurrent workers never block on or double-claim the same run (`RunRepository.claimNext`).
- The database stores enum constant names (UPPER_CASE) for `run_status` and `stage` — bind
  `stage.name()`, cast with `:status::run_status` where the column is a Postgres enum. The API shows
  lowercase via `@JsonValue` (`RunStatus`). `stage` is `NULL` until the first event (V3).
- Rows map with `.query(Run.class)`, no `RowMapper`.
- Dry-run new SQL in a rolled-back psql transaction before relying on it.

## Style

- Formatter: `control-plane/eclipse-formatter.xml` — 100 columns, long method chains one call per
  line. It never joins lines and never touches SQL inside text blocks, so format those by hand.
- Constructor injection with `final` fields and `final` parameters; `var` for locals.
- Javadoc follows the comment rule in `CLAUDE.md`: only on complex methods, explaining *why* —
  the constraint, race, or decision behind the code. Never on getters, simple delegations, or
  obvious methods. A short `//` line above the method is enough when one sentence says it. The
  tone to match (`RunService`):

  ```java
  // Single statement, so no @Transactional: the ownership check and the write are one UPDATE.
  public Run complete(final UUID runId, final String workerId, final CompletionStatus status) {
  ```

- After a change, `./gradlew compileJava -q` must pass.
