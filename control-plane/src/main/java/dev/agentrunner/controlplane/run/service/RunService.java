package dev.agentrunner.controlplane.run.service;

import org.springframework.stereotype.Service;

import dev.agentrunner.controlplane.run.entity.Run;
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

}
