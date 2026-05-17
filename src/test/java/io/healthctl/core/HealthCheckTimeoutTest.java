package io.healthctl.core;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckTimeoutTest {

    @Test
    void shouldCreateTimeoutWithDefaults() {
        HealthCheckTimeout timeout = HealthCheckTimeout.of(Duration.ofSeconds(5));

        assertEquals(Duration.ofSeconds(5), timeout.getDuration());
        assertTrue(timeout.isFailOnTimeout());
        assertNotNull(timeout.getTimeoutMessage());
        assertTrue(timeout.getTimeoutMessage().contains("5000ms"));
    }

    @Test
    void shouldCreateTimeoutWithCustomMessage() {
        HealthCheckTimeout timeout = HealthCheckTimeout.builder()
                .duration(Duration.ofMillis(500))
                .timeoutMessage("Custom timeout message")
                .failOnTimeout(false)
                .build();

        assertEquals(Duration.ofMillis(500), timeout.getDuration());
        assertFalse(timeout.isFailOnTimeout());
        assertEquals("Custom timeout message", timeout.getTimeoutMessage());
    }

    @Test
    void shouldThrowWhenDurationIsNull() {
        assertThrows(NullPointerException.class, () ->
                HealthCheckTimeout.builder().build()
        );
    }

    @Test
    void shouldProduceReadableToString() {
        HealthCheckTimeout timeout = HealthCheckTimeout.of(Duration.ofSeconds(3));
        String str = timeout.toString();
        assertTrue(str.contains("HealthCheckTimeout"));
        assertTrue(str.contains("failOnTimeout=true"));
    }
}
