package io.healthctl.core;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a performance baseline for a health check,
 * capturing expected latency and success rate thresholds.
 */
public class HealthCheckBaseline {

    private final String checkId;
    private final double expectedSuccessRate;
    private final long expectedMaxLatencyMs;
    private final Map<String, String> metadata;
    private final Instant capturedAt;

    public HealthCheckBaseline(String checkId, double expectedSuccessRate,
                               long expectedMaxLatencyMs, Map<String, String> metadata) {
        if (checkId == null || checkId.isBlank()) {
            throw new IllegalArgumentException("checkId must not be blank");
        }
        if (expectedSuccessRate < 0.0 || expectedSuccessRate > 1.0) {
            throw new IllegalArgumentException("expectedSuccessRate must be between 0.0 and 1.0");
        }
        if (expectedMaxLatencyMs < 0) {
            throw new IllegalArgumentException("expectedMaxLatencyMs must be non-negative");
        }
        this.checkId = checkId;
        this.expectedSuccessRate = expectedSuccessRate;
        this.expectedMaxLatencyMs = expectedMaxLatencyMs;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
        this.capturedAt = Instant.now();
    }

    public String getCheckId() { return checkId; }
    public double getExpectedSuccessRate() { return expectedSuccessRate; }
    public long getExpectedMaxLatencyMs() { return expectedMaxLatencyMs; }
    public Map<String, String> getMetadata() { return metadata; }
    public Instant getCapturedAt() { return capturedAt; }

    public boolean isLatencyWithinBaseline(long actualLatencyMs) {
        return actualLatencyMs <= expectedMaxLatencyMs;
    }

    public boolean isSuccessRateWithinBaseline(double actualSuccessRate) {
        return actualSuccessRate >= expectedSuccessRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HealthCheckBaseline)) return false;
        HealthCheckBaseline that = (HealthCheckBaseline) o;
        return Objects.equals(checkId, that.checkId) &&
               Double.compare(expectedSuccessRate, that.expectedSuccessRate) == 0 &&
               expectedMaxLatencyMs == that.expectedMaxLatencyMs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(checkId, expectedSuccessRate, expectedMaxLatencyMs);
    }

    @Override
    public String toString() {
        return "HealthCheckBaseline{checkId='" + checkId + "', successRate=" + expectedSuccessRate +
               ", maxLatencyMs=" + expectedMaxLatencyMs + ", capturedAt=" + capturedAt + "}";
    }
}
