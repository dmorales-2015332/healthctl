package io.healthctl.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages quarantined health checks — checks that have repeatedly failed
 * and should be temporarily excluded from normal scheduling.
 */
public class HealthCheckQuarantineManager {

    private final Map<String, QuarantineEntry> quarantined = new ConcurrentHashMap<>();
    private final int failureThreshold;
    private final Duration quarantineDuration;

    public HealthCheckQuarantineManager(int failureThreshold, Duration quarantineDuration) {
        if (failureThreshold <= 0) throw new IllegalArgumentException("failureThreshold must be > 0");
        if (quarantineDuration == null || quarantineDuration.isNegative()) {
            throw new IllegalArgumentException("quarantineDuration must be positive");
        }
        this.failureThreshold = failureThreshold;
        this.quarantineDuration = quarantineDuration;
    }

    public void recordFailure(String checkId) {
        quarantined.compute(checkId, (id, entry) -> {
            if (entry == null) entry = new QuarantineEntry();
            entry.incrementFailures();
            if (entry.getFailureCount() >= failureThreshold && !entry.isQuarantined()) {
                entry.quarantine(Instant.now().plus(quarantineDuration));
            }
            return entry;
        });
    }

    public void recordSuccess(String checkId) {
        quarantined.remove(checkId);
    }

    public boolean isQuarantined(String checkId) {
        QuarantineEntry entry = quarantined.get(checkId);
        if (entry == null || !entry.isQuarantined()) return false;
        if (Instant.now().isAfter(entry.getReleasedAt())) {
            quarantined.remove(checkId);
            return false;
        }
        return true;
    }

    public Optional<Instant> getQuarantineExpiry(String checkId) {
        QuarantineEntry entry = quarantined.get(checkId);
        if (entry == null || !entry.isQuarantined()) return Optional.empty();
        return Optional.of(entry.getReleasedAt());
    }

    public Set<String> getQuarantinedChecks() {
        Instant now = Instant.now();
        quarantined.entrySet().removeIf(e -> e.getValue().isQuarantined() && now.isAfter(e.getValue().getReleasedAt()));
        return quarantined.keySet();
    }

    public void release(String checkId) {
        quarantined.remove(checkId);
    }

    public int getFailureCount(String checkId) {
        QuarantineEntry entry = quarantined.get(checkId);
        return entry == null ? 0 : entry.getFailureCount();
    }

    private static class QuarantineEntry {
        private int failureCount = 0;
        private Instant releasedAt = null;

        void incrementFailures() { failureCount++; }
        int getFailureCount() { return failureCount; }
        boolean isQuarantined() { return releasedAt != null; }
        void quarantine(Instant until) { this.releasedAt = until; }
        Instant getReleasedAt() { return releasedAt; }
    }
}
