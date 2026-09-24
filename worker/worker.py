"""Creates a worker to claim runs from the control plane."""

import sys
from typing import Any

import requests

REQUEST_TIMEOUT_SECONDS = 5


def claim_run(
    session: requests.Session, base_url: str, worker_id: str
) -> dict[str, Any] | None:
    """Claim the next queued run, or return None when there is no work."""
    response = session.post(
        f"{base_url}/api/v1/workers/{worker_id}/claims",
        timeout=REQUEST_TIMEOUT_SECONDS,
    )
    if response.status_code == 204:
        return None
    response.raise_for_status()
    return response.json()


def post_event(
    session: requests.Session,
    base_url: str,
    worker_id: str,
    run_id: str,
    stage: str,
    message: str,
) -> None:
    """Update the stage of a run and create a run event."""
    event_body = {"stage": stage, "message": message}
    response = session.post(
        f"{base_url}/api/v1/workers/{worker_id}/runs/{run_id}/events",
        json=event_body,
        timeout=REQUEST_TIMEOUT_SECONDS,
    )
    response.raise_for_status()


def complete_run(
    session: requests.Session, base_url: str, worker_id: str, run_id: str, status: str
) -> None:
    completion_body = {"status": status}
    response = session.post(
        f"{base_url}/api/v1/workers/{worker_id}/runs/{run_id}/completion",
        json=completion_body,
        timeout=REQUEST_TIMEOUT_SECONDS,
    )
    response.raise_for_status()


def main() -> int:
    session = requests.Session()
    # run = claim_run(session, "http://127.0.0.1:8080", "worker-1")
    # print(run)

    post_event(
        session,
        "http://127.0.0.1:8080",
        "worker-1",
        "db2db0a1-d9dc-479f-8dc1-d22f468c62c6",
        "plan",
        "Moving to Planning stage",
    )

    complete_run(
        session,
        "http://127.0.0.1:8080",
        "worker-1",
        "db2db0a1-d9dc-479f-8dc1-d22f468c62c6",
        "succeeded",
    )

    return 0


if __name__ == "__main__":
    sys.exit(main())
