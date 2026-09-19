package dev.agentrunner.controlplane.run.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RunStage {
    PLAN("plan"), IMPLEMENT("implement"), VERIFY("verify");

    private final String stage;

    RunStage(final String stage) {
        this.stage = stage;
    }

    @JsonValue
    public String getStage() {
        return this.stage;
    }
}
