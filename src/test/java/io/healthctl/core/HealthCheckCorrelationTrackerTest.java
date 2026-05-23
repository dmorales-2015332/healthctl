package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckCorrelationTrackerTest {

    private HealthCheckCorrelationTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new HealthCheckCorrelationTracker();
    }

    @Test
    void shouldStartAndTrackCorrelation() {
        HealthCheckCorrelation correlation = tracker.startCorrelation("api-check");

        assertNotNull(correlation);
        assertNotNull(correlation.getCorrelationId());
        assertEquals("api-check", correlation.getCheckName());
        assertEquals(1, tracker.activeCount());
    }

    @Test
    void shouldStartChildCorrelationWithParent() {
        HealthCheckCorrelation parent = tracker.startCorrelation("parent-check");
        HealthCheckCorrelation child = tracker.startCorrelation("child-check", parent.getCorrelationId());

        assertTrue(child.getParentCorrelationId().isPresent());
        assertEquals(parent.getCorrelationId(), child.getParentCorrelationId().get());
        assertEquals(2, tracker.activeCount());
    }

    @Test
    void shouldCompleteCorrelationWithResult() {
        HealthCheckCorrelation correlation = tracker.startCorrelation("db-check");
        HealthCheckResult result = HealthCheckResult.healthy("db-check", "Connected");

        tracker.completeCorrelation(correlation.getCorrelationId(), result);

        HealthCheckCorrelation tracked = tracker.getCorrelation(correlation.getCorrelationId());
        assertNotNull(tracked);
        assertTrue(tracked.isCompleted());
        assertTrue(tracked.getResult().isPresent());
    }

    @Test
    void shouldRemoveCorrelation() {
        HealthCheckCorrelation correlation = tracker.startCorrelation("svc");
        assertEquals(1, tracker.activeCount());

        tracker.removeCorrelation(correlation.getCorrelationId());
        assertEquals(0, tracker.activeCount());
        assertNull(tracker.getCorrelation(correlation.getCorrelationId()));
    }

    @Test
    void shouldReturnImmutableSnapshotOfActiveCorrelations() {
        tracker.startCorrelation("check-a");
        tracker.startCorrelation("check-b");

        var snapshot = tracker.getActiveCorrelations();
        assertEquals(2, snapshot.size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.put("x", null));
    }

    @Test
    void shouldClearAllCorrelations() {
        tracker.startCorrelation("a");
        tracker.startCorrelation("b");
        tracker.startCorrelation("c");

        tracker.clear();
        assertEquals(0, tracker.activeCount());
    }
}
