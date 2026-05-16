package io.healthctl.core;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry that manages per-service {@link HealthCheckRateLimiter} instances,
 * allowing different services to have independent rate limit configurations.
 */
public class HealthCheckRateLimiterRegistry {

    private static final int DEFAULT_MAX_CHECKS = 10;
    private static final long DEFAULT_WINDOW_MILLIS = 60_000L;

    private final ConcurrentHashMap<String, HealthCheckRateLimiter> registry = new ConcurrentHashMap<>();
    private final int defaultMaxChecks;
    private final long defaultWindowMillis;

    public HealthCheckRateLimiterRegistry() {
        this(DEFAULT_MAX_CHECKS, DEFAULT_WINDOW_MILLIS);
    }

    public HealthCheckRateLimiterRegistry(int defaultMaxChecks, long defaultWindowMillis) {
        this.defaultMaxChecks = defaultMaxChecks;
        this.defaultWindowMillis = defaultWindowMillis;
    }

    /**
     * Registers a custom rate limiter for the given service.
     */
    public void register(String serviceName, HealthCheckRateLimiter limiter) {
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("serviceName must not be null or blank");
        }
        registry.put(serviceName, limiter);
    }

    /**
     * Returns the rate limiter for a service, creating a default one if absent.
     */
    public HealthCheckRateLimiter getOrCreate(String serviceName) {
        return registry.computeIfAbsent(serviceName,
                key -> new HealthCheckRateLimiter(defaultMaxChecks, defaultWindowMillis));
    }

    /**
     * Checks whether a permit can be acquired for the given service.
     */
    public boolean tryAcquire(String serviceName) {
        return getOrCreate(serviceName).tryAcquire(serviceName);
    }

    /**
     * Removes the rate limiter entry for a service.
     */
    public void deregister(String serviceName) {
        registry.remove(serviceName);
    }

    public boolean isRegistered(String serviceName) {
        return registry.containsKey(serviceName);
    }
}
