package dev.agentrunner.controlplane.run.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RunRequest(
    @NotBlank(message = "Task cannot be blank")
    @Size(max = 3000)
    String task
){}
