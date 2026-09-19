package dev.agentrunner.controlplane.run.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RunStatus {
    QUEUED("queued"), RUNNING("running"), SUCCEEDED("succeeded"), FAILED("failed");

    private final String status;

    RunStatus(final String status) {
        this.status = status;
    }

    @JsonValue
    public String getStatus() {
        return this.status;
    }
}
