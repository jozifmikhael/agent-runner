package dev.agentrunner.controlplane.run.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import dev.agentrunner.controlplane.run.entity.RunEvent;
import dev.agentrunner.controlplane.run.entity.RunStage;

@Repository
public class RunEventRepository {

    private static final String RUN_EVENT_COLUMNS = "id, run_id, stage, message, created_at";

    private final JdbcClient client;

    public RunEventRepository(final JdbcClient client) {
        this.client = client;
    }

    public RunEvent createRunEvent(final UUID runId, final RunStage stage, final String message) {
        var sql = """
                INSERT INTO run_events (run_id, stage, message)
                VALUES (:runId, :stage, :message)
                RETURNING %s
                """.formatted(RUN_EVENT_COLUMNS);

        return client.sql(sql)
                .param("runId", runId)
                .param("stage", stage.name())
                .param("message", message)
                .query(RunEvent.class)
                .single();
    }

    public List<RunEvent> findByRunId(final UUID runId) {
        var sql = """
                SELECT %s
                FROM run_events
                WHERE run_id = :runId
                ORDER BY id
                """.formatted(RUN_EVENT_COLUMNS);

        return client.sql(sql)
                .param("runId", runId)
                .query(RunEvent.class)
                .list();
    }
}
