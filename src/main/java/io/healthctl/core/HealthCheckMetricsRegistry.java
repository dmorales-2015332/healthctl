package io.healthctl.core;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Central registry that holds {@link HealthCheckMetrics} instances
 * keyed by health-check name. Thread-safe.
 */
public class HealthCheckMetricsRegistry {

    private final ConcurrentMap<String, HealthCheckMetrics> registry = new ConcurrentHashMap<>();

    /**
     * Returns the metrics object for the given name, creating it if absent.
     */
    public HealthCheckMetrics getOrCreate(String checkName) {
        return registry.computeIfAbsent(checkName, HealthCheckMetrics::new);
    }

    /**
     * Records a result against the appropriate metrics bucket.
     *
     * @param result    the completed health-check result
     * @param latencyMs elapsed time in milliseconds for this execution
     */
    public void record(HealthCheckResult result, long latencyMs) {
        if (result == null) throw new IllegalArgumentException("result must not be null");
        HealthCheckMetrics metrics = getOrCreate(result.getCheckName());
        if (result.isHealthy()) {
            metrics.recordSuccess(latencyMs);
        } else {
            metrics.recordFailure(latencyMs);
        }
    }

    /**
     * Returns an unmodifiable view of all tracked metrics.
     */
    public Collection<HealthCheckMetrics> all() {
        return Collections.unmodifiableCollection(registry.values());
    }

    /**
     * Retrieves metrics for a specific check name, or {@code null} if not present.
     */
    public HealthCheckMetrics get(String checkName) {
        return registry.get(checkName);
    }

    /**
     * Removes all recorded metrics (useful for testing).
     */
    public void clear() {
        registry.clear();
    }
}
