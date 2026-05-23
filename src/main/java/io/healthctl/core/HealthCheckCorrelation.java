package io.healthctl.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Represents a single correlated health check execution with timing
 * and optional parent linkage for tracing dependency chains.
 */
public class HealthCheckCorrelation {

    private final String correlationId;
    private final String checkName;
    private final Instant startedAt;
    private final String parentCorrelationId;

    private HealthCheckResult result;
    private Instant completedAt;

    public HealthCheckCorrelation(String correlationId, String checkName, Instant startedAt) {
        this(correlationId, checkName, startedAt, null);
    }

    public HealthCheckCorrelation(String correlationId, String checkName,
                                   Instant startedAt, String parentCorrelationId) {
        this.correlationId = correlationId;
        this.checkName = checkName;
        this.startedAt = startedAt;
        this.parentCorrelationId = parentCorrelationId;
    }

    public void complete(HealthCheckResult result, Instant completedAt) {
        this.result = result;
        this.completedAt = completedAt;
    }

    public boolean isCompleted() {
        return completedAt != null;
    }

    public Optional<Duration> getDuration() {
        if (completedAt == null) return Optional.empty();
        return Optional.of(Duration.between(startedAt, completedAt));
    }

    public String getCorrelationId() { return correlationId; }
    public String getCheckName() { return checkName; }
    public Instant getStartedAt() { return startedAt; }
    public Optional<String> getParentCorrelationId() { return Optional.ofNullable(parentCorrelationId); }
    public Optional<HealthCheckResult> getResult() { return Optional.ofNullable(result); }
    public Optional<Instant> getCompletedAt() { return Optional.ofNullable(completedAt); }

    @Override
    public String toString() {
        return "HealthCheckCorrelation{id='" + correlationId + "', check='" + checkName +
                "', completed=" + isCompleted() + "}";
    }
}
