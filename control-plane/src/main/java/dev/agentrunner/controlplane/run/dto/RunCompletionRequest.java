package dev.agentrunner.controlplane.run.dto;

import dev.agentrunner.controlplane.run.entity.CompletionStatus;
import jakarta.validation.constraints.NotNull;

public record RunCompletionRequest(@NotNull CompletionStatus status) {
}
