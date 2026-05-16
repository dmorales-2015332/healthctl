package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckHistoryAnalyzerTest {

    private HealthCheckHistory history;
    private HealthCheckHistoryAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        history = new HealthCheckHistory("svc-b", 20);
        analyzer = new HealthCheckHistoryAnalyzer(history);
    }

    private HealthCheckResult healthy() {
        return new HealthCheckResult("svc-b", HealthCheckResult.Status.HEALTHY, "ok", Instant.now());
    }

    private HealthCheckResult unhealthy() {
        return new HealthCheckResult("svc-b", HealthCheckResult.Status.UNHEALTHY, "err", Instant.now());
    }

    @Test
    void shouldDetectConsistentlyUnhealthy() {
        history.record(healthy());
        history.record(unhealthy());
        history.record(unhealthy());
        history.record(unhealthy());
        assertTrue(analyzer.isConsistentlyUnhealthy(3));
    }

    @Test
    void shouldNotDetectConsistentlyUnhealthyWhenHealthyPresent() {
        history.record(unhealthy());
        history.record(healthy());
        history.record(unhealthy());
        assertFalse(analyzer.isConsistentlyUnhealthy(3));
    }

    @Test
    void shouldDetectConsistentlyHealthy() {
        for (int i = 0; i < 4; i++) history.record(healthy());
        assertTrue(analyzer.isConsistentlyHealthy(4));
    }

    @Test
    void shouldReturnFalseWhenWindowLargerThanHistory() {
        history.record(healthy());
        assertFalse(analyzer.isConsistentlyHealthy(5));
    }

    @Test
    void shouldCountStatusFlips() {
        history.record(healthy());
        history.record(unhealthy());
        history.record(unhealthy());
        history.record(healthy());
        history.record(unhealthy());
        assertEquals(3, analyzer.countStatusFlips());
    }

    @Test
    void shouldReturnZeroFlipsForSingleEntry() {
        history.record(healthy());
        assertEquals(0, analyzer.countStatusFlips());
    }

    @Test
    void shouldRejectNullHistory() {
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckHistoryAnalyzer(null));
    }
}
