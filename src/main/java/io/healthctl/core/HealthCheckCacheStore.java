package io.healthctl.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches the most recent {@link HealthCheckResult} for each service to avoid
 * redundant executions within a configurable TTL window.
 */
public class HealthCheckCacheStore {

    private static final class CacheEntry {
        final HealthCheckResult result;
        final Instant expiresAt;

        CacheEntry(HealthCheckResult result, Duration ttl) {
            this.result = result;
            this.expiresAt = Instant.now().plus(ttl);
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Duration defaultTtl;

    public HealthCheckCacheStore(Duration defaultTtl) {
        if (defaultTtl == null || defaultTtl.isNegative() || defaultTtl.isZero()) {
            throw new IllegalArgumentException("TTL must be a positive duration");
        }
        this.defaultTtl = defaultTtl;
    }

    /**
     * Store a result for the given service name using the default TTL.
     */
    public void put(String serviceName, HealthCheckResult result) {
        put(serviceName, result, defaultTtl);
    }

    /**
     * Store a result for the given service name using a custom TTL.
     */
    public void put(String serviceName, HealthCheckResult result, Duration ttl) {
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("Service name must not be blank");
        }
        cache.put(serviceName, new CacheEntry(result, ttl));
    }

    /**
     * Retrieve a non-expired cached result, if present.
     */
    public Optional<HealthCheckResult> get(String serviceName) {
        CacheEntry entry = cache.get(serviceName);
        if (entry == null || entry.isExpired()) {
            cache.remove(serviceName);
            return Optional.empty();
        }
        return Optional.of(entry.result);
    }

    /**
     * Explicitly invalidate the cached result for a service.
     */
    public void invalidate(String serviceName) {
        cache.remove(serviceName);
    }

    /**
     * Remove all expired entries from the cache.
     */
    public void evictExpired() {
        cache.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    public int size() {
        return cache.size();
    }
}
