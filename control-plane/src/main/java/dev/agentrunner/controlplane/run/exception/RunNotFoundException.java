package dev.agentrunner.controlplane.run.exception;

import java.util.UUID;

public class RunNotFoundException extends RuntimeException {

    public RunNotFoundException(final UUID id) {
        super("Run with id: %s does not exist".formatted(id));
    }
}
