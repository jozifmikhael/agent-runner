package dev.agentrunner.controlplane.run.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import dev.agentrunner.controlplane.run.dto.RunRequest;
import dev.agentrunner.controlplane.run.dto.RunResponse;
import dev.agentrunner.controlplane.run.service.RunService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/runs")
public class RunController {

    private final RunService runService;

    public RunController(final RunService runService) {
        this.runService = runService;
    }

    @PostMapping
    public ResponseEntity<RunResponse> createRun(@RequestBody @Valid RunRequest request) {
        var run = runService.createRun(request.task());

        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(run.id())
                .toUri();

        return ResponseEntity.created(uri).body(RunResponse.from(run));
    }

    @GetMapping("/{runUuid}")
    public ResponseEntity<RunResponse> getRun(@PathVariable UUID runUuid) {
        var run = runService.findById(runUuid);

        return ResponseEntity.ok(RunResponse.from(run));
    }

}
