package io.healthctl.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckResultTest {

    private HealthCheckResult makeResult(HealthCheckResult.Status status, int exitCode) {
        return new HealthCheckResult("svc-test", status, exitCode, "output", Instant.now(), 42L);
    }

    @Test
    void healthyResult_isHealthy() {
        HealthCheckResult result = makeResult(HealthCheckResult.Status.HEALTHY, 0);
        assertTrue(result.isHealthy());
        assertEquals(HealthCheckResult.Status.HEALTHY, result.getStatus());
    }

    @Test
    void unhealthyResult_isNotHealthy() {
        HealthCheckResult result = makeResult(HealthCheckResult.Status.UNHEALTHY, 1);
        assertFalse(result.isHealthy());
    }

    @Test
    void unknownResult_isNotHealthy() {
        HealthCheckResult result = makeResult(HealthCheckResult.Status.UNKNOWN, -1);
        assertFalse(result.isHealthy());
    }

    @Test
    void fieldsAreStoredCorrectly() {
        Instant now = Instant.now();
        HealthCheckResult result = new HealthCheckResult("my-service", HealthCheckResult.Status.HEALTHY,
                0, "OK", now, 100L);
        assertEquals("my-service", result.getServiceName());
        assertEquals(0, result.getExitCode());
        assertEquals("OK", result.getOutput());
        assertEquals(now, result.getTimestamp());
        assertEquals(100L, result.getDurationMillis());
    }

    @Test
    void toStringContainsServiceName() {
        HealthCheckResult result = makeResult(HealthCheckResult.Status.HEALTHY, 0);
        assertTrue(result.toString().contains("svc-test"));
    }
}
