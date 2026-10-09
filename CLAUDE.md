# agent-runner

A distributed agent execution platform. A control plane owns run state and hands tasks to a fleet of
workers; workers execute an agent loop and report progress; a dashboard (later) shows runs in flight
and the points where a human has to approve.

Component-specific rules load from `.claude/rules/` when working in that component.

## Commands

From the repo root:

- Postgres: `docker compose up -d` (reads `.env`; see README)
- psql: `docker compose exec postgres psql -U postgres -d agent_runner`

From `control-plane/` (use `gradlew.bat` on Windows):

- Run: `./gradlew bootRun` — binds `127.0.0.1:8080`, applies Flyway migrations on startup
- Compile check: `./gradlew compileJava -q`
- Test: `./gradlew test`

From `worker/`, inside its virtual environment (`.venv`):

- Install: `pip install -r requirements.txt`
- Run: `python worker.py <worker-id>` — each worker gets a distinct id

## Architecture

- **Control plane** (`control-plane/`) — Java 21, Spring Boot 4.1.x, Gradle (Kotlin DSL). Owns all
  run state.
- **Worker** (`worker/`) — Python. Claims a run, executes stages, reports back over HTTP.
- **Postgres** — Docker Compose; schema owned by Flyway.
- **Dashboard** — not built yet (milestone 4).

Workers never talk to each other or to the database directly — only to the control plane over HTTP.

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

## Git workflow

- `main` holds finished milestones. `develop` is the integration branch and the GitHub default.
- Branch from `develop` as `feature/…`, `fix/…`, or `chore/…`; open a PR into `develop`.
- `develop` merges into `main` when a milestone is complete.
- Prefix commit messages and PR titles with the milestone: `M1: …`.
- Work items are GitHub issues. Reference them in PRs (`Closes #n`).
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
