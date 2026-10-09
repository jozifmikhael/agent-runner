---
paths:
  - "worker/**"
---

# Worker conventions

Examples below are excerpts from `worker/worker.py`.

- **Keep the worker dumb until milestone 5.** It exists to prove the coordination logic; stages are
  fake until real LLM calls arrive.
- **HTTP only.** The worker talks to the control plane's `/api/v1/workers/{workerId}/...`
  endpoints and nothing else — never the database, never another worker.
- **One function per endpoint**, taking the shared `requests.Session`, `base_url`, and
  `worker_id` explicitly (no globals). Every request carries `timeout=REQUEST_TIMEOUT_SECONDS`.
- **Map status codes deliberately before `raise_for_status()`.** `204` from a claim means no work;
  `409` means this worker no longer holds the run, so raise `RunLostError` and stop work on that
  run — do not retry the same request:

  ```python
  if response.status_code == 409:
      raise RunLostError(
          f"Post run event failed, run {run_id} is not owned by {worker_id}"
      )
  response.raise_for_status()
  ```

- Type hints on every signature, using built-in generics and `|` unions:

  ```python
  def claim_run(
      session: requests.Session, base_url: str, worker_id: str
  ) -> dict[str, Any] | None:
  ```

- Code should be self-documenting through names and structure. Docstrings (the Python equivalent
  of Javadoc) go only on complex functions, and explain *why*: the constraint, race, or decision
  behind the code. Never restate what the code already says; never add one to a simple function
  whose name and signature say it all. Format per PEP 257: triple double quotes, a one-line
  summary ending in a period, and a blank line before any detail. Inline `#` comments follow the
  same rule: only for a non-obvious *why*.

  Redundant — restates the name and signature, so leave it out:

  ```python
  def complete_run(...) -> None:
      """Update run status to succeeded/failed."""
  ```

  The tone to match — says what the code cannot (illustrative):

  ```python
  class RunLostError(Exception):
      """Raised on 409: the control plane no longer counts this worker as the run's holder.

      Not retried, because the run is already finished or held by another worker; retrying
      cannot win it back.
      """
  ```

  ```python
  # 204 is the normal idle case, not an error, so check it before raise_for_status().
  if response.status_code == 204:
      return None
  ```
- Dependencies are pinned in `requirements.txt`; install inside `worker/.venv`.
