package io.healthctl.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckTest {

    @Test
    void shouldReturnSuccessWhenCommandExitsZero() {
        HealthCheck check = new HealthCheck("echo-check", "echo healthy", 5);
        HealthCheckResult result = check.execute();

        assertTrue(result.isSuccess());
        assertEquals("echo-check", result.getName());
        assertTrue(result.getOutput().contains("healthy"));
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getFinishedAt());
    }

    @Test
    void shouldReturnFailureWhenCommandExitsNonZero() {
        HealthCheck check = new HealthCheck("fail-check", "exit 1", 5);
        HealthCheckResult result = check.execute();

        assertFalse(result.isSuccess());
        assertTrue(result.getOutput().contains("Exit code 1"));
    }

    @Test
    void shouldReturnFailureOnTimeout() {
        HealthCheck check = new HealthCheck("timeout-check", "sleep 10", 1);
        HealthCheckResult result = check.execute();

        assertFalse(result.isSuccess());
        assertTrue(result.getOutput().contains("timed out"));
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new HealthCheck("", "echo ok", 5));
    }

    @Test
    void shouldThrowWhenCommandIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new HealthCheck("check", "  ", 5));
    }

    @Test
    void shouldThrowWhenTimeoutIsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> new HealthCheck("check", "echo ok", 0));
        assertThrows(IllegalArgumentException.class,
                () -> new HealthCheck("check", "echo ok", -1));
    }

    @Test
    void shouldCaptureMultiLineOutput() {
        HealthCheck check = new HealthCheck("multiline", "printf 'line1\\nline2\\nline3'", 5);
        HealthCheckResult result = check.execute();

        assertTrue(result.isSuccess());
        assertTrue(result.getOutput().contains("line1"));
        assertTrue(result.getOutput().contains("line3"));
    }
}
