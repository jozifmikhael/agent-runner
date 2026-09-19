package dev.agentrunner.controlplane.run.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CompletionStatus {
    SUCCEEDED("succeeded"), FAILED("failed");

    private final String status;

    CompletionStatus(final String status) {
        this.status = status;
    }

    @JsonValue
    public String getStatus() {
        return this.status;
    }
}
