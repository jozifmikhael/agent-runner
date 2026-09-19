package dev.agentrunner.controlplane.run.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import dev.agentrunner.controlplane.run.entity.RunWithEvents;

public record RunDetailResponse(@JsonUnwrapped RunResponse run, List<RunEventResponse> events) {
    public static RunDetailResponse from(final RunWithEvents runWithEvents) {
        var events = runWithEvents.events()
                .stream()
                .map(RunEventResponse::from)
                .toList();

        return new RunDetailResponse(RunResponse.from(runWithEvents.run()), events);
    }
}
