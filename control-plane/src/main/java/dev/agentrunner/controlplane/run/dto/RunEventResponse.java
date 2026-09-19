package dev.agentrunner.controlplane.run.dto;

import java.time.Instant;
import java.util.UUID;

import dev.agentrunner.controlplane.run.entity.RunEvent;
import dev.agentrunner.controlplane.run.entity.RunStage;

public record RunEventResponse(UUID runId, RunStage stage, String message, Instant createdAt) {
    public static RunEventResponse from(final RunEvent runEvent) {
        return new RunEventResponse(
                runEvent.runId(),
                runEvent.stage(),
                runEvent.message(),
                runEvent.createdAt());
    }
}
