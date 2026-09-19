package dev.agentrunner.controlplane.run.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.agentrunner.controlplane.run.entity.Run;
import dev.agentrunner.controlplane.run.entity.RunEvent;
import dev.agentrunner.controlplane.run.entity.RunStage;
import dev.agentrunner.controlplane.run.exception.RunNotFoundException;
import dev.agentrunner.controlplane.run.exception.RunNotHeldException;
import dev.agentrunner.controlplane.run.repository.RunEventRepository;
import dev.agentrunner.controlplane.run.repository.RunRepository;

@Service
public class RunService {

    private final RunRepository runRepository;
    private final RunEventRepository runEventRepository;

    public RunService(final RunRepository runRepository,
            final RunEventRepository runEventRepository) {
        this.runRepository = runRepository;
        this.runEventRepository = runEventRepository;
    }

    public Run createRun(final String task) {
        return runRepository.createRun(task);
    }

    public Run findById(final UUID runUuid) {
        return runRepository.findById(runUuid).orElseThrow(() -> new RunNotFoundException(runUuid));
    }

    public Optional<Run> claimNext(final String workerId) {
        return runRepository.claimNext(workerId);
    }

    @Transactional
    public RunEvent recordEvent(final UUID runId, final String workerId, final RunStage stage,
            final String message) {
        var rowsAffected = runRepository.updateStage(runId, workerId, stage);
        if (rowsAffected == 0) {
            throw notHeldOrNotFound(runId, workerId);
        }

        return runEventRepository.createRunEvent(runId, stage, message);
    }

    // Only called after a zero-row update, so this read never decides the write.
    private RuntimeException notHeldOrNotFound(final UUID runId, final String workerId) {
        return runRepository.existsById(runId)
                ? new RunNotHeldException(runId, workerId)
                : new RunNotFoundException(runId);
    }
}
