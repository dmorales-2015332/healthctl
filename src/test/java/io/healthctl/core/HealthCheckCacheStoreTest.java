package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckCacheStoreTest {

    private HealthCheckCacheStore store;

    @BeforeEach
    void setUp() {
        store = new HealthCheckCacheStore(Duration.ofSeconds(10));
    }

    private HealthCheckResult successResult(String service) {
        return new HealthCheckResult(service, true, "OK", Instant.now());
    }

    @Test
    void shouldStoreAndRetrieveCachedResult() {
        HealthCheckResult result = successResult("db");
        store.put("db", result);

        Optional<HealthCheckResult> cached = store.get("db");
        assertTrue(cached.isPresent());
        assertEquals(result, cached.get());
    }

    @Test
    void shouldReturnEmptyForUnknownService() {
        Optional<HealthCheckResult> cached = store.get("unknown");
        assertFalse(cached.isPresent());
    }

    @Test
    void shouldExpireEntryAfterCustomTtl() throws InterruptedException {
        HealthCheckResult result = successResult("cache");
        store.put("cache", result, Duration.ofMillis(50));

        assertTrue(store.get("cache").isPresent());
        Thread.sleep(100);
        assertFalse(store.get("cache").isPresent());
    }

    @Test
    void shouldInvalidateEntry() {
        store.put("svc", successResult("svc"));
        store.invalidate("svc");
        assertFalse(store.get("svc").isPresent());
    }

    @Test
    void shouldEvictExpiredEntries() throws InterruptedException {
        store.put("a", successResult("a"), Duration.ofMillis(50));
        store.put("b", successResult("b"), Duration.ofSeconds(60));

        Thread.sleep(100);
        store.evictExpired();

        assertEquals(1, store.size());
        assertTrue(store.get("b").isPresent());
    }

    @Test
    void shouldRejectNullOrBlankServiceName() {
        HealthCheckResult result = successResult("x");
        assertThrows(IllegalArgumentException.class, () -> store.put(null, result));
        assertThrows(IllegalArgumentException.class, () -> store.put("  ", result));
    }

    @Test
    void shouldRejectInvalidTtlOnConstruction() {
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckCacheStore(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckCacheStore(Duration.ofSeconds(-1)));
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckCacheStore(null));
    }
}
