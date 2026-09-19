package dev.agentrunner.controlplane.run.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.agentrunner.controlplane.run.dto.RunResponse;
import dev.agentrunner.controlplane.run.service.RunService;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/workers")
public class WorkerController {

    private final RunService runService;

    public WorkerController(final RunService runService) {
        this.runService = runService;
    }

    @PostMapping("/{workerId}/claims")
    public ResponseEntity<RunResponse> claimNext(
            @PathVariable @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "Invalid pattern for worker ID, must contain only letters, digits, and hyphens") @Size(max = 64) String workerId) {
        return runService.claimNext(workerId)
                .map((run) -> ResponseEntity.ok(RunResponse.from(run)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
