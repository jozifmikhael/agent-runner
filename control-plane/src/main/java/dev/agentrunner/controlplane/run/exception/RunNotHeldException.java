package dev.agentrunner.controlplane.run.exception;

import java.util.UUID;

public class RunNotHeldException extends RuntimeException {

    public RunNotHeldException(final UUID runId, final String workerId) {
        super("Run %s is not held by worker %s".formatted(runId, workerId));
    }
}
