package io.healthctl.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckTimeoutExecutorTest {

    private HealthCheckTimeoutExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new HealthCheckTimeoutExecutor();
    }

    @AfterEach
    void tearDown() {
        executor.shutdown();
    }

    @Test
    void shouldReturnSuccessWhenTaskCompletesInTime() throws Exception {
        HealthCheckTimeout timeout = HealthCheckTimeout.of(Duration.ofSeconds(2));

        HealthCheckResult result = executor.execute(
                () -> HealthCheckResult.healthy("ok"),
                timeout
        );

        assertTrue(result.isHealthy());
        assertEquals("ok", result.getMessage());
    }

    @Test
    void shouldReturnFailureWhenTaskExceedsTimeout() {
        HealthCheckTimeout timeout = HealthCheckTimeout.of(Duration.ofMillis(100));

        HealthCheckResult result = executor.execute(() -> {
            Thread.sleep(500);
            return HealthCheckResult.healthy("should not reach");
        }, timeout);

        assertFalse(result.isHealthy());
        assertTrue(result.getMessage().contains("timed out") || result.getMessage().contains("100ms"));
    }

    @Test
    void shouldReturnUnknownOnTimeoutWhenFailOnTimeoutIsFalse() {
        HealthCheckTimeout timeout = HealthCheckTimeout.builder()
                .duration(Duration.ofMillis(100))
                .failOnTimeout(false)
                .build();

        HealthCheckResult result = executor.execute(() -> {
            Thread.sleep(500);
            return HealthCheckResult.healthy("should not reach");
        }, timeout);

        assertNotNull(result);
        assertFalse(result.isHealthy());
    }

    @Test
    void shouldHandleExceptionThrownByTask() {
        HealthCheckTimeout timeout = HealthCheckTimeout.of(Duration.ofSeconds(2));

        HealthCheckResult result = executor.execute(() -> {
            throw new RuntimeException("unexpected error");
        }, timeout);

        assertFalse(result.isHealthy());
        assertTrue(result.getMessage().contains("unexpected error"));
    }
}
