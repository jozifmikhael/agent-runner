package dev.agentrunner.controlplane.run.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import dev.agentrunner.controlplane.run.entity.Run;

@Repository
public class RunRepository {

    private final JdbcClient client;

    public RunRepository(final JdbcClient client) {
        this.client = client;
    }

    public Run createRun(final String task) {
        var sql = "INSERT INTO runs (task) VALUES(:task) RETURNING id, task, status, stage, attempt, worker_id, claimed_at, created_at, updated_at";

        return client.sql(sql)
                .param("task", task)
                .query(Run.class)
                .single();
    }
}
