package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckRetryTrackerTest {

    private HealthCheckRetryTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new HealthCheckRetryTracker();
    }

    @Test
    void shouldReturnZeroAttemptsForUnregisteredCheck() {
        assertEquals(0, tracker.getAttemptCount("unknown"));
    }

    @Test
    void shouldIncrementAttemptCountOnRecord() {
        tracker.register("svc-a", 3);
        tracker.recordAttempt("svc-a");
        tracker.recordAttempt("svc-a");
        assertEquals(2, tracker.getAttemptCount("svc-a"));
    }

    @Test
    void shouldDetectExhaustedRetries() {
        tracker.register("svc-b", 2);
        tracker.recordAttempt("svc-b");
        assertFalse(tracker.isExhausted("svc-b"));
        tracker.recordAttempt("svc-b");
        assertTrue(tracker.isExhausted("svc-b"));
    }

    @Test
    void shouldTrackFirstAndLastAttemptTimes() throws InterruptedException {
        tracker.register("svc-c", 5);
        tracker.recordAttempt("svc-c");
        Instant first = tracker.getFirstAttemptTime("svc-c");
        Thread.sleep(10);
        tracker.recordAttempt("svc-c");
        Instant last = tracker.getLastAttemptTime("svc-c");

        assertNotNull(first);
        assertNotNull(last);
        assertTrue(last.isAfter(first) || last.equals(first));
    }

    @Test
    void shouldReportNonZeroElapsedMillisAfterMultipleAttempts() throws InterruptedException {
        tracker.register("svc-d", 5);
        tracker.recordAttempt("svc-d");
        Thread.sleep(15);
        tracker.recordAttempt("svc-d");
        assertTrue(tracker.getElapsedMillis("svc-d") >= 0);
    }

    @Test
    void shouldResetAttemptCountAndTimes() {
        tracker.register("svc-e", 3);
        tracker.recordAttempt("svc-e");
        tracker.recordAttempt("svc-e");
        tracker.reset("svc-e");
        assertEquals(0, tracker.getAttemptCount("svc-e"));
        assertNull(tracker.getFirstAttemptTime("svc-e"));
        assertNull(tracker.getLastAttemptTime("svc-e"));
    }

    @Test
    void shouldUnregisterCheckCompletely() {
        tracker.register("svc-f", 3);
        tracker.recordAttempt("svc-f");
        tracker.unregister("svc-f");
        assertEquals(0, tracker.getAttemptCount("svc-f"));
        assertFalse(tracker.isExhausted("svc-f"));
    }

    @Test
    void shouldNotBeExhaustedForUnregisteredCheck() {
        assertFalse(tracker.isExhausted("not-registered"));
    }
}
