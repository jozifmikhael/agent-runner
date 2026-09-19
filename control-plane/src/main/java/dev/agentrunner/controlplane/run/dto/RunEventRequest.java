package dev.agentrunner.controlplane.run.dto;

import dev.agentrunner.controlplane.run.entity.RunStage;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RunEventRequest(@NotNull RunStage stage, @Size(max = 256) String message) {
}
