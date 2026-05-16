package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckFilterTest {

    private HealthCheckResult healthy;
    private HealthCheckResult unhealthy;
    private HealthCheckResult unknown;

    @BeforeEach
    void setUp() {
        healthy = new HealthCheckResult("db-service", 0, "OK", 120L);
        unhealthy = new HealthCheckResult("api-service", 1, "Connection refused", 300L);
        unknown = new HealthCheckResult("cache-service", -1, "Timeout", 5000L);
    }

    @Test
    void filterAll_returnsAllResults() {
        HealthCheckFilter filter = HealthCheckFilter.all();
        List<HealthCheckResult> result = filter.apply(List.of(healthy, unhealthy, unknown));
        assertEquals(3, result.size());
    }

    @Test
    void filterUnhealthyOnly_returnsOnlyUnhealthy() {
        HealthCheckFilter filter = HealthCheckFilter.unhealthyOnly();
        List<HealthCheckResult> result = filter.apply(List.of(healthy, unhealthy, unknown));
        assertEquals(1, result.size());
        assertFalse(result.get(0).isHealthy());
        assertEquals("api-service", result.get(0).getServiceName());
    }

    @Test
    void filterHealthy_returnsOnlyHealthy() {
        HealthCheckFilter filter = new HealthCheckFilter(HealthCheckFilter.StatusFilter.HEALTHY);
        List<HealthCheckResult> result = filter.apply(List.of(healthy, unhealthy, unknown));
        assertEquals(1, result.size());
        assertTrue(result.get(0).isHealthy());
    }

    @Test
    void filterByServiceNamePattern_returnsMatchingResults() {
        HealthCheckFilter filter = new HealthCheckFilter(HealthCheckFilter.StatusFilter.ALL, ".*-service");
        List<HealthCheckResult> result = filter.apply(List.of(healthy, unhealthy, unknown));
        assertEquals(3, result.size());
    }

    @Test
    void filterByServiceNamePattern_excludesNonMatching() {
        HealthCheckFilter filter = new HealthCheckFilter(HealthCheckFilter.StatusFilter.ALL, "db.*");
        List<HealthCheckResult> result = filter.apply(List.of(healthy, unhealthy, unknown));
        assertEquals(1, result.size());
        assertEquals("db-service", result.get(0).getServiceName());
    }

    @Test
    void apply_withNullList_returnsEmpty() {
        HealthCheckFilter filter = HealthCheckFilter.all();
        List<HealthCheckResult> result = filter.apply(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void summary_allHealthy_correctCounts() {
        HealthCheckSummary summary = new HealthCheckSummary(List.of(healthy));
        assertTrue(summary.isAllHealthy());
        assertEquals(1, summary.getHealthyCount());
        assertEquals(0, summary.getUnhealthyCount());
    }

    @Test
    void summary_mixedResults_notAllHealthy() {
        HealthCheckSummary summary = new HealthCheckSummary(List.of(healthy, unhealthy, unknown));
        assertFalse(summary.isAllHealthy());
        assertEquals(3, summary.getTotalChecks());
        assertEquals(1, summary.getHealthyCount());
        assertEquals(1, summary.getUnhealthyCount());
        assertEquals(1, summary.getUnknownCount());
    }
}
