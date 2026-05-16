package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckHistoryTest {

    private HealthCheckHistory history;

    @BeforeEach
    void setUp() {
        history = new HealthCheckHistory("svc-a", 5);
    }

    private HealthCheckResult healthy() {
        return new HealthCheckResult("svc-a", HealthCheckResult.Status.HEALTHY, "ok", Instant.now());
    }

    private HealthCheckResult unhealthy() {
        return new HealthCheckResult("svc-a", HealthCheckResult.Status.UNHEALTHY, "fail", Instant.now());
    }

    @Test
    void shouldRecordAndRetrieveEntries() {
        history.record(healthy());
        history.record(unhealthy());
        assertEquals(2, history.size());
    }

    @Test
    void shouldEvictOldestWhenCapacityExceeded() {
        for (int i = 0; i < 6; i++) history.record(healthy());
        assertEquals(5, history.size());
    }

    @Test
    void shouldReturnLatestEntry() {
        history.record(healthy());
        HealthCheckResult last = unhealthy();
        history.record(last);
        assertEquals(HealthCheckResult.Status.UNHEALTHY, history.getLatest().getStatus());
    }

    @Test
    void shouldCalculateSuccessRate() {
        history.record(healthy());
        history.record(healthy());
        history.record(unhealthy());
        assertEquals(2.0 / 3.0, history.successRate(), 0.001);
    }

    @Test
    void shouldRejectNullResult() {
        assertThrows(IllegalArgumentException.class, () -> history.record(null));
    }

    @Test
    void shouldRejectBlankServiceName() {
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckHistory("", 10));
    }

    @Test
    void shouldRejectNonPositiveMaxEntries() {
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckHistory("svc", 0));
    }
}
