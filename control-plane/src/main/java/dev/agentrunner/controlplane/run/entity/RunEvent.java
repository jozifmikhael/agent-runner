package dev.agentrunner.controlplane.run.entity;

import java.time.Instant;
import java.util.UUID;

public record RunEvent(long id, UUID runId, RunStage stage, String message, Instant createdAt) {
}
