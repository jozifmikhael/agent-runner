package dev.agentrunner.controlplane.run.entity;

import java.util.List;

public record RunWithEvents(Run run, List<RunEvent> events) {
}
