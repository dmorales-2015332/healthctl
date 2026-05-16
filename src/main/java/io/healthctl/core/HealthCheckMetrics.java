package io.healthctl.core;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks runtime metrics for a named health check,
 * including success/failure counts and average latency.
 */
public class HealthCheckMetrics {

    private final String checkName;
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong totalLatencyMs = new AtomicLong(0);
    private final AtomicInteger executionCount = new AtomicInteger(0);
    private volatile Instant lastExecutedAt;

    public HealthCheckMetrics(String checkName) {
        if (checkName == null || checkName.isBlank()) {
            throw new IllegalArgumentException("checkName must not be null or blank");
        }
        this.checkName = checkName;
    }

    public void recordSuccess(long latencyMs) {
        successCount.incrementAndGet();
        record(latencyMs);
    }

    public void recordFailure(long latencyMs) {
        failureCount.incrementAndGet();
        record(latencyMs);
    }

    private void record(long latencyMs) {
        totalLatencyMs.addAndGet(latencyMs);
        executionCount.incrementAndGet();
        lastExecutedAt = Instant.now();
    }

    public String getCheckName() { return checkName; }

    public int getSuccessCount() { return successCount.get(); }

    public int getFailureCount() { return failureCount.get(); }

    public int getExecutionCount() { return executionCount.get(); }

    public double getAverageLatencyMs() {
        int count = executionCount.get();
        return count == 0 ? 0.0 : (double) totalLatencyMs.get() / count;
    }

    public Instant getLastExecutedAt() { return lastExecutedAt; }

    @Override
    public String toString() {
        return String.format(
            "HealthCheckMetrics{name='%s', success=%d, failure=%d, avgLatencyMs=%.2f, lastRun=%s}",
            checkName, getSuccessCount(), getFailureCount(), getAverageLatencyMs(), lastExecutedAt
        );
    }
}
