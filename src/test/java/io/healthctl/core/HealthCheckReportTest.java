package io.healthctl.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckReportTest {

    private HealthCheckResult result(String service, HealthCheckResult.Status status, long durationMs) {
        return new HealthCheckResult(service, status, status == HealthCheckResult.Status.HEALTHY ? 0 : 1,
                "", Instant.now(), durationMs);
    }

    @Test
    void emptyReport_returnsZeroCounts() {
        HealthCheckReport report = new HealthCheckReport(List.of());
        assertEquals(0, report.totalChecks());
        assertEquals(0, report.healthyCount());
        assertEquals(0, report.unhealthyCount());
        assertEquals(0.0, report.averageDurationMillis());
    }

    @Test
    void countsHealthyAndUnhealthy() {
        List<HealthCheckResult> results = List.of(
                result("svc-a", HealthCheckResult.Status.HEALTHY, 10),
                result("svc-a", HealthCheckResult.Status.UNHEALTHY, 20),
                result("svc-b", HealthCheckResult.Status.HEALTHY, 30)
        );
        HealthCheckReport report = new HealthCheckReport(results);
        assertEquals(3, report.totalChecks());
        assertEquals(2, report.healthyCount());
        assertEquals(1, report.unhealthyCount());
    }

    @Test
    void countByServiceGroupsCorrectly() {
        List<HealthCheckResult> results = List.of(
                result("svc-a", HealthCheckResult.Status.HEALTHY, 10),
                result("svc-a", HealthCheckResult.Status.HEALTHY, 10),
                result("svc-b", HealthCheckResult.Status.UNHEALTHY, 10)
        );
        Map<String, Long> counts = new HealthCheckReport(results).countByService();
        assertEquals(2L, counts.get("svc-a"));
        assertEquals(1L, counts.get("svc-b"));
    }

    @Test
    void averageDurationIsCorrect() {
        List<HealthCheckResult> results = List.of(
                result("svc-a", HealthCheckResult.Status.HEALTHY, 100),
                result("svc-b", HealthCheckResult.Status.HEALTHY, 200)
        );
        HealthCheckReport report = new HealthCheckReport(results);
        assertEquals(150.0, report.averageDurationMillis());
    }

    @Test
    void toStringContainsSummary() {
        HealthCheckReport report = new HealthCheckReport(List.of(
                result("svc", HealthCheckResult.Status.HEALTHY, 50)
        ));
        assertTrue(report.toString().contains("total=1"));
    }
}
