package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckSlaTrackerTest {

    private HealthCheckSlaTracker tracker;
    private static final String CHECK = "db-ping";

    @BeforeEach
    void setUp() {
        tracker = new HealthCheckSlaTracker(99.0, Duration.ofMinutes(5));
    }

    @Test
    void shouldReturnFullUptimeWhenNoRecords() {
        assertEquals(100.0, tracker.getUptimePercent(CHECK));
    }

    @Test
    void shouldNotBreachSlaWhenAllChecksPass() {
        Instant now = Instant.now();
        for (int i = 0; i < 100; i++) {
            tracker.record(CHECK, true, now.plusSeconds(i));
        }
        assertEquals(100.0, tracker.getUptimePercent(CHECK), 0.001);
        assertFalse(tracker.isSlaBreached(CHECK));
    }

    @Test
    void shouldBreachSlaWhenTooManyFailures() {
        Instant now = Instant.now();
        for (int i = 0; i < 95; i++) {
            tracker.record(CHECK, true, now.plusSeconds(i));
        }
        for (int i = 95; i < 100; i++) {
            tracker.record(CHECK, false, now.plusSeconds(i));
        }
        double uptime = tracker.getUptimePercent(CHECK);
        assertEquals(95.0, uptime, 0.001);
        assertTrue(tracker.isSlaBreached(CHECK));
    }

    @Test
    void shouldTrackTotalsAndFailures() {
        Instant now = Instant.now();
        tracker.record(CHECK, true, now);
        tracker.record(CHECK, false, now.plusSeconds(1));
        tracker.record(CHECK, true, now.plusSeconds(2));
        assertEquals(3L, tracker.getTotalChecks(CHECK));
        assertEquals(1L, tracker.getFailureCount(CHECK));
    }

    @Test
    void shouldResetTrackingForCheck() {
        Instant now = Instant.now();
        tracker.record(CHECK, false, now);
        tracker.reset(CHECK);
        assertEquals(100.0, tracker.getUptimePercent(CHECK));
        assertEquals(0L, tracker.getTotalChecks(CHECK));
    }

    @Test
    void shouldRejectInvalidTargetUptime() {
        assertThrows(IllegalArgumentException.class, () ->
                new HealthCheckSlaTracker(-1.0, Duration.ofMinutes(1)));
        assertThrows(IllegalArgumentException.class, () ->
                new HealthCheckSlaTracker(101.0, Duration.ofMinutes(1)));
    }

    @Test
    void shouldExposeConfiguredTarget() {
        assertEquals(99.0, tracker.getTargetUptimePercent(), 0.001);
    }
}
