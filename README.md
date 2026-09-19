# agent-runner

A distributed agent execution platform. A control plane owns run state and hands tasks to a fleet
of workers; workers execute an agent loop and report progress back over HTTP.

## Architecture

| Component | Stack | Responsibility |
|---|---|---|
| **Control plane** (`control-plane/`) | Java 21, Spring Boot 4.1, Postgres | Owns all run state; hands out work via an atomic claim |
| **Worker** (`worker/`) | Python | Claims a run, executes stages, reports back over HTTP |
| **Postgres** | Docker Compose | Current state and append-only event history; schema owned by Flyway |

Workers never talk to each other or to the database directly — only to the control plane over HTTP.

## Prerequisites

- JDK 21
- Docker Desktop
- Python 3.12 (worker)

## Getting started

1. Create `.env` at the repo root (read by Docker Compose):

   ```env
   DATABASE=agent_runner
   DATABASE_USER=postgres
   DATABASE_PASSWORD=admin
   ```

2. Start Postgres:

   ```sh
   docker compose up -d
   ```

3. Run the control plane:

   ```sh
   cd control-plane
   ./gradlew bootRun
   ```

   It binds to `http://127.0.0.1:8080` and applies Flyway migrations on startup.

## API

All paths are under `/api/v1`. Client endpoints manage runs; worker endpoints claim and report.
Errors use Problem Details (RFC 9457).

### Client

| Method & path | Success | Purpose |
|---|---|---|
| `POST /runs` | `201` + `Location` | Submit a task |
| `GET /runs/{runId}` | `200` | Read a run with its event history |

### Worker

| Method & path | Success | Purpose |
|---|---|---|
| `POST /workers/{workerId}/claims` | `200` / `204` | Claim the next queued run |
| `POST /workers/{workerId}/runs/{runId}/events` | `201` | Report progress on a stage |
| `POST /workers/{workerId}/runs/{runId}/completion` | `200` | Set the terminal status |

## Project structure

```
agent-runner/
  control-plane/      Java / Spring Boot — REST API, claim logic, Flyway migrations
  worker/             Python — claims and executes runs
  docker-compose.yml  Postgres
```

## Roadmap

Built in milestones, each triggered by the previous one breaking.

1. Control plane + worker + atomic claim — *current*
2. Heartbeats, lease expiry, requeue
3. A real queue
4. Dashboard with live updates
5. Real LLM calls in the worker
6. Evaluation gates and human approval
7. Deployment, tracing, metrics
