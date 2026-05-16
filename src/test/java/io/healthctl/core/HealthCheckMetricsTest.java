package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckMetricsTest {

    private HealthCheckMetrics metrics;
    private HealthCheckMetricsRegistry registry;

    @BeforeEach
    void setUp() {
        metrics = new HealthCheckMetrics("db-check");
        registry = new HealthCheckMetricsRegistry();
    }

    @Test
    void initialCountsAreZero() {
        assertEquals(0, metrics.getSuccessCount());
        assertEquals(0, metrics.getFailureCount());
        assertEquals(0, metrics.getExecutionCount());
        assertEquals(0.0, metrics.getAverageLatencyMs());
        assertNull(metrics.getLastExecutedAt());
    }

    @Test
    void recordSuccessUpdatesCountsAndLatency() {
        metrics.recordSuccess(100);
        metrics.recordSuccess(200);
        assertEquals(2, metrics.getSuccessCount());
        assertEquals(0, metrics.getFailureCount());
        assertEquals(2, metrics.getExecutionCount());
        assertEquals(150.0, metrics.getAverageLatencyMs());
        assertNotNull(metrics.getLastExecutedAt());
    }

    @Test
    void recordFailureUpdatesCountsAndLatency() {
        metrics.recordFailure(50);
        assertEquals(0, metrics.getSuccessCount());
        assertEquals(1, metrics.getFailureCount());
        assertEquals(50.0, metrics.getAverageLatencyMs());
    }

    @Test
    void blankNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckMetrics(""));
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckMetrics(null));
    }

    @Test
    void registryGetOrCreateReturnsSameInstance() {
        HealthCheckMetrics first = registry.getOrCreate("svc-check");
        HealthCheckMetrics second = registry.getOrCreate("svc-check");
        assertSame(first, second);
    }

    @Test
    void registryRecordsHealthyResult() {
        HealthCheckResult result = new HealthCheckResult("svc-check", true, "OK");
        registry.record(result, 80);
        HealthCheckMetrics m = registry.get("svc-check");
        assertNotNull(m);
        assertEquals(1, m.getSuccessCount());
        assertEquals(0, m.getFailureCount());
    }

    @Test
    void registryRecordsUnhealthyResult() {
        HealthCheckResult result = new HealthCheckResult("svc-check", false, "timeout");
        registry.record(result, 300);
        HealthCheckMetrics m = registry.get("svc-check");
        assertEquals(0, m.getSuccessCount());
        assertEquals(1, m.getFailureCount());
    }

    @Test
    void registryClearRemovesAll() {
        registry.getOrCreate("a");
        registry.getOrCreate("b");
        registry.clear();
        assertTrue(registry.all().isEmpty());
    }
}
