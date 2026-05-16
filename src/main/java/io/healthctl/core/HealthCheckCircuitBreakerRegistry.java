package io.healthctl.core;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry that manages circuit breakers per service.
 */
public class HealthCheckCircuitBreakerRegistry {

    private static final int DEFAULT_FAILURE_THRESHOLD = 3;
    private static final long DEFAULT_RESET_TIMEOUT_MS = 30_000L;

    private final Map<String, HealthCheckCircuitBreaker> registry = new ConcurrentHashMap<>();
    private final int defaultFailureThreshold;
    private final long defaultResetTimeoutMillis;

    public HealthCheckCircuitBreakerRegistry() {
        this(DEFAULT_FAILURE_THRESHOLD, DEFAULT_RESET_TIMEOUT_MS);
    }

    public HealthCheckCircuitBreakerRegistry(int defaultFailureThreshold, long defaultResetTimeoutMillis) {
        this.defaultFailureThreshold = defaultFailureThreshold;
        this.defaultResetTimeoutMillis = defaultResetTimeoutMillis;
    }

    public HealthCheckCircuitBreaker getOrCreate(String serviceName) {
        return registry.computeIfAbsent(serviceName,
                name -> new HealthCheckCircuitBreaker(name, defaultFailureThreshold, defaultResetTimeoutMillis));
    }

    public HealthCheckCircuitBreaker register(String serviceName, int failureThreshold, long resetTimeoutMillis) {
        HealthCheckCircuitBreaker breaker = new HealthCheckCircuitBreaker(serviceName, failureThreshold, resetTimeoutMillis);
        registry.put(serviceName, breaker);
        return breaker;
    }

    public boolean contains(String serviceName) {
        return registry.containsKey(serviceName);
    }

    public void remove(String serviceName) {
        registry.remove(serviceName);
    }

    public Map<String, HealthCheckCircuitBreaker> getAll() {
        return Collections.unmodifiableMap(registry);
    }

    public void resetAll() {
        registry.values().forEach(HealthCheckCircuitBreaker::reset);
    }

    public int size() {
        return registry.size();
    }
}
