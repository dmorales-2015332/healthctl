package io.healthctl.core;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiter for health check executions to prevent thundering herd
 * and excessive resource consumption during high-frequency scheduling.
 */
public class HealthCheckRateLimiter {

    private final int maxChecksPerWindow;
    private final long windowDurationMillis;
    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public HealthCheckRateLimiter(int maxChecksPerWindow, long windowDurationMillis) {
        if (maxChecksPerWindow <= 0) throw new IllegalArgumentException("maxChecksPerWindow must be positive");
        if (windowDurationMillis <= 0) throw new IllegalArgumentException("windowDurationMillis must be positive");
        this.maxChecksPerWindow = maxChecksPerWindow;
        this.windowDurationMillis = windowDurationMillis;
    }

    /**
     * Attempts to acquire a permit for the given service name.
     *
     * @param serviceName the name of the service being checked
     * @return true if the check is allowed, false if rate limit exceeded
     */
    public boolean tryAcquire(String serviceName) {
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("serviceName must not be null or blank");
        }
        long now = Instant.now().toEpochMilli();
        WindowCounter counter = counters.compute(serviceName, (key, existing) -> {
            if (existing == null || now - existing.windowStart >= windowDurationMillis) {
                return new WindowCounter(now);
            }
            return existing;
        });
        return counter.count.incrementAndGet() <= maxChecksPerWindow;
    }

    /**
     * Returns the current count for a service within the active window.
     */
    public int getCurrentCount(String serviceName) {
        WindowCounter counter = counters.get(serviceName);
        if (counter == null) return 0;
        long now = Instant.now().toEpochMilli();
        if (now - counter.windowStart >= windowDurationMillis) return 0;
        return counter.count.get();
    }

    /**
     * Resets the rate limiter state for a specific service.
     */
    public void reset(String serviceName) {
        counters.remove(serviceName);
    }

    private static class WindowCounter {
        final long windowStart;
        final AtomicInteger count = new AtomicInteger(0);

        WindowCounter(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
