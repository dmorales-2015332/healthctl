package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckGracePeriodManagerTest {

    private HealthCheckGracePeriodManager manager;

    @BeforeEach
    void setUp() {
        manager = new HealthCheckGracePeriodManager();
    }

    @Test
    void shouldBeInGracePeriodAfterRegistration() {
        manager.register("db-check", Duration.ofMinutes(5));
        assertTrue(manager.isInGracePeriod("db-check"));
    }

    @Test
    void shouldNotBeInGracePeriodForUnregisteredCheck() {
        assertFalse(manager.isInGracePeriod("unknown-check"));
    }

    @Test
    void shouldNotBeInGracePeriodAfterExpiry() throws InterruptedException {
        manager.register("fast-check", Duration.ofMillis(50));
        Thread.sleep(100);
        assertFalse(manager.isInGracePeriod("fast-check"));
    }

    @Test
    void shouldReturnExpiryInstantForActiveGracePeriod() {
        Duration duration = Duration.ofMinutes(10);
        Instant before = Instant.now();
        manager.register("api-check", duration);
        Instant expiry = manager.getExpiry("api-check");
        assertNotNull(expiry);
        assertTrue(expiry.isAfter(before));
    }

    @Test
    void shouldReturnNullExpiryForUnregisteredCheck() {
        assertNull(manager.getExpiry("no-such-check"));
    }

    @Test
    void shouldCancelGracePeriod() {
        manager.register("cache-check", Duration.ofMinutes(1));
        assertTrue(manager.isInGracePeriod("cache-check"));
        manager.cancel("cache-check");
        assertFalse(manager.isInGracePeriod("cache-check"));
    }

    @Test
    void shouldRejectNullCheckName() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.register(null, Duration.ofSeconds(10)));
    }

    @Test
    void shouldRejectBlankCheckName() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.register("  ", Duration.ofSeconds(10)));
    }

    @Test
    void shouldRejectZeroDuration() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.register("svc-check", Duration.ZERO));
    }

    @Test
    void shouldRejectNegativeDuration() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.register("svc-check", Duration.ofSeconds(-5)));
    }

    @Test
    void shouldTrackMultipleGracePeriods() {
        manager.register("check-a", Duration.ofMinutes(1));
        manager.register("check-b", Duration.ofMinutes(2));
        assertEquals(2, manager.size());
        assertTrue(manager.isInGracePeriod("check-a"));
        assertTrue(manager.isInGracePeriod("check-b"));
    }
}
