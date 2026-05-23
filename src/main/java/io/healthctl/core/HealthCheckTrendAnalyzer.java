package io.healthctl.core;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Analyzes health check result trends over a sliding window to detect
 * degradation patterns such as increasing failure rates or latency spikes.
 */
public class HealthCheckTrendAnalyzer {

    public enum Trend {
        IMPROVING, STABLE, DEGRADING, CRITICAL
    }

    private final int windowSize;
    private final double degradingThreshold;
    private final double criticalThreshold;

    public HealthCheckTrendAnalyzer(int windowSize, double degradingThreshold, double criticalThreshold) {
        if (windowSize < 2) throw new IllegalArgumentException("Window size must be at least 2");
        if (degradingThreshold < 0 || degradingThreshold > 1)
            throw new IllegalArgumentException("Degrading threshold must be between 0 and 1");
        if (criticalThreshold < degradingThreshold)
            throw new IllegalArgumentException("Critical threshold must be >= degrading threshold");
        this.windowSize = windowSize;
        this.degradingThreshold = degradingThreshold;
        this.criticalThreshold = criticalThreshold;
    }

    /**
     * Evaluates the trend for a given check name based on recent results.
     *
     * @param results ordered list of recent HealthCheckResult (oldest first)
     * @return computed Trend
     */
    public Trend evaluate(List<HealthCheckResult> results) {
        if (results == null || results.isEmpty()) return Trend.STABLE;

        List<HealthCheckResult> window = results.size() > windowSize
                ? results.subList(results.size() - windowSize, results.size())
                : results;

        long failures = window.stream().filter(r -> !r.isSuccess()).count();
        double failureRate = (double) failures / window.size();

        if (failureRate >= criticalThreshold) return Trend.CRITICAL;
        if (failureRate >= degradingThreshold) return Trend.DEGRADING;

        // Check if recent half is better than earlier half
        int mid = window.size() / 2;
        List<HealthCheckResult> earlier = window.subList(0, mid);
        List<HealthCheckResult> recent = window.subList(mid, window.size());

        double earlierFailRate = earlier.isEmpty() ? 0 :
                (double) earlier.stream().filter(r -> !r.isSuccess()).count() / earlier.size();
        double recentFailRate = recent.isEmpty() ? 0 :
                (double) recent.stream().filter(r -> !r.isSuccess()).count() / recent.size();

        if (recentFailRate < earlierFailRate) return Trend.IMPROVING;
        return Trend.STABLE;
    }

    /**
     * Produces a per-service trend map from a grouped result set.
     */
    public Map<String, Trend> evaluateAll(Map<String, List<HealthCheckResult>> resultsByService) {
        return resultsByService.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> evaluate(e.getValue())));
    }

    public int getWindowSize() { return windowSize; }
    public double getDegradingThreshold() { return degradingThreshold; }
    public double getCriticalThreshold() { return criticalThreshold; }
}
