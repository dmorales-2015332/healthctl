package io.healthctl.core;

import java.util.List;

/**
 * Provides analytical insights derived from a {@link HealthCheckHistory}.
 */
public class HealthCheckHistoryAnalyzer {

    private final HealthCheckHistory history;

    public HealthCheckHistoryAnalyzer(HealthCheckHistory history) {
        if (history == null) throw new IllegalArgumentException("history must not be null");
        this.history = history;
    }

    /**
     * Returns true when the last {@code window} results are all unhealthy,
     * indicating a sustained outage.
     */
    public boolean isConsistentlyUnhealthy(int window) {
        List<HealthCheckResult> entries = history.getEntries();
        if (entries.size() < window) return false;
        return entries.subList(entries.size() - window, entries.size())
                .stream()
                .allMatch(r -> r.getStatus() != HealthCheckResult.Status.HEALTHY);
    }

    /**
     * Returns true when the last {@code window} results are all healthy.
     */
    public boolean isConsistentlyHealthy(int window) {
        List<HealthCheckResult> entries = history.getEntries();
        if (entries.size() < window) return false;
        return entries.subList(entries.size() - window, entries.size())
                .stream()
                .allMatch(r -> r.getStatus() == HealthCheckResult.Status.HEALTHY);
    }

    /**
     * Counts how many times the health status changed (flap detection).
     */
    public int countStatusFlips() {
        List<HealthCheckResult> entries = history.getEntries();
        if (entries.size() < 2) return 0;
        int flips = 0;
        HealthCheckResult.Status prev = entries.get(0).getStatus();
        for (int i = 1; i < entries.size(); i++) {
            HealthCheckResult.Status curr = entries.get(i).getStatus();
            if (curr != prev) {
                flips++;
                prev = curr;
            }
        }
        return flips;
    }

    public double successRate() {
        return history.successRate();
    }

    public HealthCheckHistory getHistory() {
        return history;
    }
}
