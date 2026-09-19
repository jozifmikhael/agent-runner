package dev.agentrunner.controlplane.run.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import dev.agentrunner.controlplane.run.entity.CompletionStatus;
import dev.agentrunner.controlplane.run.entity.Run;
import dev.agentrunner.controlplane.run.entity.RunStage;

@Repository
public class RunRepository {

    private static final String RUN_COLUMNS = "id, task, status, stage, attempt, worker_id, "
            + "claimed_at, created_at, updated_at";

    private final JdbcClient client;

    public RunRepository(final JdbcClient client) {
        this.client = client;
    }

    public Run createRun(final String task) {
        var sql = """
                INSERT INTO runs (task)
                VALUES (:task)
                RETURNING %s
                """.formatted(RUN_COLUMNS);

        return client.sql(sql)
                .param("task", task)
                .query(Run.class)
                .single();
    }

    public Optional<Run> findById(final UUID runUuid) {
        var sql = """
                SELECT %s
                FROM runs
                WHERE id = :id
                """.formatted(RUN_COLUMNS);

        return client.sql(sql)
                .param("id", runUuid)
                .query(Run.class)
                .optional();
    }

    public Optional<Run> claimNext(final String workerId) {
        var sql = """
                UPDATE runs
                SET status = 'RUNNING',
                    worker_id = :workerId,
                    claimed_at = now(),
                    attempt = attempt + 1,
                    updated_at = now()
                WHERE id = (
                    SELECT id
                    FROM runs
                    WHERE status = 'QUEUED'
                    ORDER BY created_at
                    LIMIT 1
                    FOR UPDATE SKIP LOCKED
                )
                RETURNING %s
                """.formatted(RUN_COLUMNS);

        return client.sql(sql)
                .param("workerId", workerId)
                .query(Run.class)
                .optional();
    }

    public boolean existsById(final UUID runId) {
        var sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM runs
                    WHERE id = :id
                )
                """;

        return client.sql(sql)
                .param("id", runId)
                .query(Boolean.class)
                .single();
    }

    public int updateStage(final UUID runId, final String workerId, final RunStage stage) {
        var sql = """
                UPDATE runs
                SET stage = :stage,
                    updated_at = now()
                WHERE id = :runId
                    AND worker_id = :workerId
                    AND status = 'RUNNING'
                """;

        return client.sql(sql)
                .param("stage", stage.name())
                .param("runId", runId)
                .param("workerId", workerId)
                .update();
    }

    public Optional<Run> complete(final UUID runId, final String workerId,
            final CompletionStatus status) {
        var sql = """
                UPDATE runs
                SET status = :status::run_status,
                    updated_at = now()
                WHERE id = :runId
                    AND worker_id = :workerId
                    AND status = 'RUNNING'
                RETURNING %s
                """.formatted(RUN_COLUMNS);

        return client.sql(sql)
                .param("status", status.name())
                .param("runId", runId)
                .param("workerId", workerId)
                .query(Run.class)
                .optional();
    }
}
