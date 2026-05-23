package io.healthctl.core;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckCorrelationTest {

    @Test
    void shouldCreateCorrelationWithRequiredFields() {
        Instant now = Instant.now();
        HealthCheckCorrelation correlation = new HealthCheckCorrelation("id-1", "db-check", now);

        assertEquals("id-1", correlation.getCorrelationId());
        assertEquals("db-check", correlation.getCheckName());
        assertEquals(now, correlation.getStartedAt());
        assertTrue(correlation.getParentCorrelationId().isEmpty());
        assertFalse(correlation.isCompleted());
    }

    @Test
    void shouldLinkParentCorrelationId() {
        HealthCheckCorrelation child = new HealthCheckCorrelation(
                "child-id", "cache-check", Instant.now(), "parent-id");

        assertTrue(child.getParentCorrelationId().isPresent());
        assertEquals("parent-id", child.getParentCorrelationId().get());
    }

    @Test
    void shouldReturnEmptyDurationWhenNotCompleted() {
        HealthCheckCorrelation correlation = new HealthCheckCorrelation("id", "svc", Instant.now());
        assertTrue(correlation.getDuration().isEmpty());
        assertTrue(correlation.getResult().isEmpty());
        assertTrue(correlation.getCompletedAt().isEmpty());
    }

    @Test
    void shouldCalculateDurationOnCompletion() throws InterruptedException {
        Instant start = Instant.now();
        HealthCheckCorrelation correlation = new HealthCheckCorrelation("id", "svc", start);
        Thread.sleep(10);
        HealthCheckResult result = HealthCheckResult.healthy("svc", "OK");
        correlation.complete(result, Instant.now());

        assertTrue(correlation.isCompleted());
        assertTrue(correlation.getDuration().isPresent());
        assertTrue(correlation.getDuration().get().compareTo(Duration.ZERO) > 0);
        assertTrue(correlation.getResult().isPresent());
    }

    @Test
    void toStringShouldContainKeyInfo() {
        HealthCheckCorrelation correlation = new HealthCheckCorrelation("abc", "my-check", Instant.now());
        String str = correlation.toString();
        assertTrue(str.contains("abc"));
        assertTrue(str.contains("my-check"));
    }
}
