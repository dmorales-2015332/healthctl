package io.healthctl.core;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks correlation IDs across health check executions to enable
 * distributed tracing and cross-check causality analysis.
 */
public class HealthCheckCorrelationTracker {

    private final Map<String, HealthCheckCorrelation> activeCorrelations = new ConcurrentHashMap<>();

    public HealthCheckCorrelation startCorrelation(String checkName) {
        String correlationId = UUID.randomUUID().toString();
        HealthCheckCorrelation correlation = new HealthCheckCorrelation(
                correlationId, checkName, Instant.now());
        activeCorrelations.put(correlationId, correlation);
        return correlation;
    }

    public HealthCheckCorrelation startCorrelation(String checkName, String parentCorrelationId) {
        String correlationId = UUID.randomUUID().toString();
        HealthCheckCorrelation correlation = new HealthCheckCorrelation(
                correlationId, checkName, Instant.now(), parentCorrelationId);
        activeCorrelations.put(correlationId, correlation);
        return correlation;
    }

    public void completeCorrelation(String correlationId, HealthCheckResult result) {
        HealthCheckCorrelation correlation = activeCorrelations.get(correlationId);
        if (correlation != null) {
            correlation.complete(result, Instant.now());
        }
    }

    public void removeCorrelation(String correlationId) {
        activeCorrelations.remove(correlationId);
    }

    public HealthCheckCorrelation getCorrelation(String correlationId) {
        return activeCorrelations.get(correlationId);
    }

    public Map<String, HealthCheckCorrelation> getActiveCorrelations() {
        return Map.copyOf(activeCorrelations);
    }

    public int activeCount() {
        return activeCorrelations.size();
    }

    public void clear() {
        activeCorrelations.clear();
    }
}
