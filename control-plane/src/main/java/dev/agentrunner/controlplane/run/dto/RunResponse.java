package dev.agentrunner.controlplane.run.dto;

import java.time.Instant;
import java.util.UUID;

import dev.agentrunner.controlplane.run.entity.Run;
import dev.agentrunner.controlplane.run.entity.RunStatus;

public record RunResponse(UUID id,
        String task,
        RunStatus status,
        String stage,
        int attempt,
        String workerId,
        Instant claimedAt,
        Instant createdAt,
        Instant updatedAt) {
    public static RunResponse from(final Run run) {
        return new RunResponse(
                run.id(),
                run.task(),
                run.status(),
                run.stage(),
                run.attempt(),
                run.workerId(),
                run.claimedAt(),
                run.createdAt(),
                run.updatedAt());
    }
}
