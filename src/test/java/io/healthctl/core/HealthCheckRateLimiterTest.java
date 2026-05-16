package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckRateLimiterTest {

    private HealthCheckRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new HealthCheckRateLimiter(3, 60_000L);
    }

    @Test
    void shouldAllowChecksWithinLimit() {
        assertTrue(rateLimiter.tryAcquire("serviceA"));
        assertTrue(rateLimiter.tryAcquire("serviceA"));
        assertTrue(rateLimiter.tryAcquire("serviceA"));
    }

    @Test
    void shouldDenyChecksExceedingLimit() {
        rateLimiter.tryAcquire("serviceB");
        rateLimiter.tryAcquire("serviceB");
        rateLimiter.tryAcquire("serviceB");
        assertFalse(rateLimiter.tryAcquire("serviceB"));
    }

    @Test
    void shouldTrackCountsIndependentlyPerService() {
        rateLimiter.tryAcquire("serviceC");
        rateLimiter.tryAcquire("serviceC");
        rateLimiter.tryAcquire("serviceC");
        assertTrue(rateLimiter.tryAcquire("serviceD"));
    }

    @Test
    void shouldReturnCurrentCountCorrectly() {
        rateLimiter.tryAcquire("serviceE");
        rateLimiter.tryAcquire("serviceE");
        assertEquals(2, rateLimiter.getCurrentCount("serviceE"));
    }

    @Test
    void shouldReturnZeroCountForUnknownService() {
        assertEquals(0, rateLimiter.getCurrentCount("unknown"));
    }

    @Test
    void shouldResetCountForService() {
        rateLimiter.tryAcquire("serviceF");
        rateLimiter.tryAcquire("serviceF");
        rateLimiter.tryAcquire("serviceF");
        rateLimiter.reset("serviceF");
        assertTrue(rateLimiter.tryAcquire("serviceF"));
    }

    @Test
    void shouldThrowOnInvalidServiceName() {
        assertThrows(IllegalArgumentException.class, () -> rateLimiter.tryAcquire(null));
        assertThrows(IllegalArgumentException.class, () -> rateLimiter.tryAcquire("  "));
    }

    @Test
    void shouldThrowOnInvalidConstructorArguments() {
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckRateLimiter(0, 1000));
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckRateLimiter(5, 0));
    }
}
