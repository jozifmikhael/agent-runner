"""Creates a worker to claim runs from the control plane."""

import argparse
import logging
import os
import sys
import time
from typing import Any

import requests

DEFAULT_BASE_URL = "http://127.0.0.1:8080"
REQUEST_TIMEOUT_SECONDS = 5
POLL_INTERVAL_SECONDS = 2
STAGE_PAUSE_SECONDS = 3
STAGES = ("plan", "implement", "verify")

log = logging.getLogger(__name__)


class RunLostError(Exception):
    """Raised on 409: the control plane no longer counts this worker as the holder.

    Not retried, because the run is already finished or held by another worker; retrying
    cannot win it back.
    """


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
    event_body = {"stage": stage, "message": message}
    response = session.post(
        f"{base_url}/api/v1/workers/{worker_id}/runs/{run_id}/events",
        json=event_body,
        timeout=REQUEST_TIMEOUT_SECONDS,
    )
    if response.status_code == 409:
        raise RunLostError(
            f"Post run event failed, run {run_id} is not owned by {worker_id}"
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
    if response.status_code == 409:
        raise RunLostError(
            f"Complete run failed, run {run_id} is not owned by {worker_id}"
        )
    response.raise_for_status()


def process_run(
    session: requests.Session, base_url: str, worker_id: str, run: dict[str, Any]
) -> None:
    run_id = run["id"]
    for stage in STAGES:
        post_event(session, base_url, worker_id, run_id, stage, f"Started {stage}")
        log.info("run %s: %s", run_id, stage)
        time.sleep(STAGE_PAUSE_SECONDS)

    complete_run(session, base_url, worker_id, run_id, "succeeded")
    log.info("run %s: succeeded", run_id)


def run_loop(session: requests.Session, base_url: str, worker_id: str) -> None:
    while True:
        try:
            run = claim_run(session, base_url, worker_id)
        except requests.ConnectionError:
            # Treated as an empty poll, so the worker survives a control plane restart.
            log.warning("control plane unreachable at %s", base_url)
            time.sleep(POLL_INTERVAL_SECONDS)
            continue

        if run is None:
            log.info("no work")
            time.sleep(POLL_INTERVAL_SECONDS)
            continue

        log.info("claimed run %s", run["id"])
        try:
            process_run(session, base_url, worker_id, run)
        except RunLostError as error:
            log.warning("%s; abandoning it", error)


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Claim runs from the control plane and execute their stages."
    )
    parser.add_argument(
        "worker_id", help="this worker's id: letters, digits, and hyphens"
    )
    args = parser.parse_args()

    logging.basicConfig(
        level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s"
    )
    base_url = os.environ.get("CONTROL_PLANE_URL", DEFAULT_BASE_URL)
    session = requests.Session()

    log.info("worker %s starting against %s", args.worker_id, base_url)
    try:
        run_loop(session, base_url, args.worker_id)
    except KeyboardInterrupt:
        log.info("worker %s shutting down", args.worker_id)

    return 0


if __name__ == "__main__":
    sys.exit(main())
