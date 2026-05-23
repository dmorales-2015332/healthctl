package io.healthctl.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks SLA compliance for health checks by monitoring uptime percentages
 * and breach counts over a configurable observation window.
 */
public class HealthCheckSlaTracker {

    private final double targetUptimePercent;
    private final Duration observationWindow;
    private final Map<String, SlaRecord> records = new ConcurrentHashMap<>();

    public HealthCheckSlaTracker(double targetUptimePercent, Duration observationWindow) {
        if (targetUptimePercent < 0.0 || targetUptimePercent > 100.0) {
            throw new IllegalArgumentException("targetUptimePercent must be between 0 and 100");
        }
        this.targetUptimePercent = targetUptimePercent;
        this.observationWindow = observationWindow;
    }

    public void record(String checkName, boolean healthy, Instant timestamp) {
        records.computeIfAbsent(checkName, k -> new SlaRecord()).record(healthy, timestamp, observationWindow);
    }

    public double getUptimePercent(String checkName) {
        SlaRecord rec = records.get(checkName);
        if (rec == null) return 100.0;
        return rec.uptimePercent();
    }

    public boolean isSlaBreached(String checkName) {
        return getUptimePercent(checkName) < targetUptimePercent;
    }

    public long getTotalChecks(String checkName) {
        SlaRecord rec = records.get(checkName);
        return rec == null ? 0L : rec.total.get();
    }

    public long getFailureCount(String checkName) {
        SlaRecord rec = records.get(checkName);
        return rec == null ? 0L : rec.failures.get();
    }

    public void reset(String checkName) {
        records.remove(checkName);
    }

    public double getTargetUptimePercent() {
        return targetUptimePercent;
    }

    private static class SlaRecord {
        final AtomicLong total = new AtomicLong(0);
        final AtomicLong failures = new AtomicLong(0);
        volatile Instant windowStart = Instant.now();

        synchronized void record(boolean healthy, Instant timestamp, Duration window) {
            if (timestamp.isAfter(windowStart.plus(window))) {
                total.set(0);
                failures.set(0);
                windowStart = timestamp;
            }
            total.incrementAndGet();
            if (!healthy) {
                failures.incrementAndGet();
            }
        }

        double uptimePercent() {
            long t = total.get();
            if (t == 0) return 100.0;
            return ((double)(t - failures.get()) / t) * 100.0;
        }
    }
}
