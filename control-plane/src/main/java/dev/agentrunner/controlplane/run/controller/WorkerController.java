package dev.agentrunner.controlplane.run.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.agentrunner.controlplane.run.dto.RunCompletionRequest;
import dev.agentrunner.controlplane.run.dto.RunEventRequest;
import dev.agentrunner.controlplane.run.dto.RunEventResponse;
import dev.agentrunner.controlplane.run.dto.RunResponse;
import dev.agentrunner.controlplane.run.service.RunService;
import dev.agentrunner.controlplane.run.validation.WorkerId;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/workers")
public class WorkerController {

    private final RunService runService;

    public WorkerController(final RunService runService) {
        this.runService = runService;
    }

    @PostMapping("/{workerId}/claims")
    public ResponseEntity<RunResponse> claimNext(@PathVariable @WorkerId String workerId) {
        return runService.claimNext(workerId)
                .map((run) -> ResponseEntity.ok(RunResponse.from(run)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/{workerId}/runs/{runId}/events")
    public ResponseEntity<RunEventResponse> recordEvent(
            @PathVariable @WorkerId String workerId,
            @PathVariable UUID runId,
            @RequestBody @Valid RunEventRequest request) {
        var event = runService.recordEvent(runId, workerId, request.stage(), request.message());

        return ResponseEntity.status(HttpStatus.CREATED).body(RunEventResponse.from(event));
    }

    @PostMapping("/{workerId}/runs/{runId}/completion")
    public ResponseEntity<RunResponse> completeRun(
            @PathVariable @WorkerId String workerId,
            @PathVariable UUID runId,
            @RequestBody @Valid RunCompletionRequest request) {
        var run = runService.complete(runId, workerId, request.status());

        return ResponseEntity.ok(RunResponse.from(run));
    }

}
