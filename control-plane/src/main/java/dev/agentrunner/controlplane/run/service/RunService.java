package dev.agentrunner.controlplane.run.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.agentrunner.controlplane.run.entity.Run;
import dev.agentrunner.controlplane.run.exception.RunNotFoundException;
import dev.agentrunner.controlplane.run.repository.RunRepository;

@Service
public class RunService {

    private final RunRepository runRepository;

    public RunService(final RunRepository runRepository) {
        this.runRepository = runRepository;
    }

    public Run createRun(final String task) {
        return runRepository.createRun(task);
    }

    public Run findById(final UUID runUuid) {
        return runRepository.findById(runUuid)
                .orElseThrow(() -> new RunNotFoundException(runUuid));
    }
}
