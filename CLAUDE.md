# agent-runner

A distributed agent execution platform: a Java control plane owns all run state and hands runs to a
fleet of Python workers over HTTP. `README.md` has setup and the API.

## Layout

- `control-plane/` — Spring Boot API, claim logic; migrations in `src/main/resources/db/migration`
- `worker/` — the Python worker, `worker.py`
- `docker-compose.yml` — Postgres

Workers never talk to each other or to the database — only to the control plane over HTTP.
Language-specific conventions load from `.claude/rules/` when working in each directory.

## Commands

Repo root:

- Postgres: `docker compose up -d` (reads `.env`; see README)
- psql: `docker compose exec postgres psql -U postgres -d agent_runner`

`control-plane/` (`./gradlew` in Git Bash, `gradlew.bat` in cmd or PowerShell):

- Run: `./gradlew bootRun` — binds `127.0.0.1:8080`, applies Flyway migrations on startup
- Compile check: `./gradlew compileJava -q`
- Tests: `./gradlew test`; one class: `./gradlew test --tests '<ClassName>'`

`worker/` (paths are Windows; on macOS/Linux use `.venv/bin/python`):

- Setup: `python -m venv .venv`, then `.venv/Scripts/python.exe -m pip install -r requirements.txt`
- Run: `.venv/Scripts/python.exe worker.py <worker-id>`, or `python worker.py <worker-id>` with
  `.venv` activated. Each worker needs a distinct id.
- Control plane URL: `CONTROL_PLANE_URL`, default `http://127.0.0.1:8080`
- Lint and format: Ruff, through the VS Code extension. There is no command-line Ruff until CI.

## Code (both languages)

What clean, DRY, and SOLID mean in this repo, concretely:

- **Names and structure carry the meaning.** Comments and docstrings explain only *why* — never
  restate the code. Per-language format is in `.claude/rules/`.
- **One job per function and class.** HTTP, state rules, and SQL live in separate layers (control
  plane) or separate small helpers (worker), so the top-level flow reads as a list of steps.
- **Pass dependencies in:** constructor injection in Java, parameters in Python. No mutable
  globals or static state.
- **Each rule, constant, or query lives in one place.** Similar-looking code may appear twice;
  extract it on the third copy, or sooner if the copies must always change together.
- **No speculative abstraction:** no interface with a single implementation, no option for a value
  that never varies, nothing built for a later milestone.
- **Name tunable values** as `UPPER_CASE` constants (timeouts, intervals, limits). No magic numbers
  or strings in logic.
- **Prefer immutable data:** Java records and `final`; Python tuples for fixed sequences.
- **Handle expected errors by name; let everything else fail loudly.** Never catch and ignore.
- **Validate at the boundary**, then trust the value inside.
- **Type every signature.**
- **Log ids, not payloads.**
- **Keep a change to its purpose.** No drive-by refactors or reformatting of unrelated code.

## Settled decisions

Do not re-litigate these without a reason that did not exist when they were made.

- **Plain SQL through `JdbcClient`, not JPA.** The claim is raw SQL with `FOR UPDATE SKIP LOCKED`;
  an ORM would obscure it.
- **Database as the queue, until it hurts.** A broker arrives in milestone 3, not before.
- **Flyway owns the schema.** Never edit an applied migration; add the next `V<n>__`.
- **`runs` holds current state, `run_events` is append-only history.** Not event sourcing: state is
  stored directly, events are supplementary.
- **Bind to localhost** until authentication exists (milestone 7). The worker id in the path is an
  *assertion*, not an authenticated identity, until then. Describe it accurately.

## Before opening a PR

- Control plane: `./gradlew compileJava -q` and `./gradlew test` pass.
- Worker: Ruff reports nothing for the changed file, and the change was run against a live
  control plane.
- The PR title is `M<n>: <what changed>`. The body says what changed, why, and how it was tested,
  and links issues with `Closes #n`.

## Git workflow

- `main` holds finished milestones; `develop` is the integration branch and the GitHub default.
  Both are protected by rulesets: changes arrive only through PRs.
- Branch from `develop` as `feature/…`, `fix/…`, or `chore/…`, and open a PR into `develop`.
- PRs into `develop` are **squash-merged**: the PR title becomes the commit and the PR body its
  message. Branch commit messages are not kept, so the title is what matters.
- After a squash merge, delete the local branch with `git branch -D`. `-d` refuses, because the
  squash commit is not the branch's commit.
- `develop` merges into `main` when a milestone is complete, through a PR merged with a **merge
  commit**. Squashing there would split the two branches' histories.
- Work items are GitHub issues.
- Never commit `.env` or `CLAUDE.local.md`.

## Milestones

Each one exists because the previous one broke. Do not build ahead.

1. Control plane + worker + atomic claim, with the API conventions — *current*
2. Heartbeats, lease expiry, requeue (triggered by: a killed worker strands a run)
3. A real queue (triggered by: polling feels wasteful)
4. Dashboard with live updates (triggered by: cannot see what is happening)
5. Real LLM calls in the worker (triggered by: fake stages get boring)
6. Evaluation gates and human approval (triggered by: the agent does something dumb)
7. Deployment, tracing, metrics (triggered by: wanting it off the laptop)
