package dev.agentrunner.controlplane.run.entity;

import java.time.Instant;
import java.util.UUID;

public record Run(UUID id,
    String task,
    RunStatus status,
    RunStage stage,
    int attempt,
    String workerId,
    Instant claimedAt,
    Instant createdAt,
    Instant updatedAt
){}
