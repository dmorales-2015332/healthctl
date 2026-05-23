package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckSnapshotComparatorTest {

    private HealthCheckSnapshotComparator comparator;

    private HealthCheckResult result(String name, String status) {
        return new HealthCheckResult(name, status, 50L, Instant.now(), "");
    }

    private HealthCheckSnapshot snapshot(List<HealthCheckResult> results) {
        return new HealthCheckSnapshot(UUID.randomUUID().toString(), Instant.now(), results, Map.of());
    }

    @BeforeEach
    void setUp() {
        comparator = new HealthCheckSnapshotComparator();
    }

    @Test
    void detectsStatusChange() {
        HealthCheckSnapshot base = snapshot(List.of(result("db", "UP")));
        HealthCheckSnapshot target = snapshot(List.of(result("db", "DOWN")));
        var diff = comparator.compare(base, target);
        assertTrue(diff.hasChanges());
        assertEquals(1, diff.changes().size());
        assertEquals("db", diff.changes().get(0).checkName());
        assertTrue(diff.changes().get(0).isDegradation());
    }

    @Test
    void detectsRecovery() {
        HealthCheckSnapshot base = snapshot(List.of(result("api", "DOWN")));
        HealthCheckSnapshot target = snapshot(List.of(result("api", "UP")));
        var diff = comparator.compare(base, target);
        assertTrue(diff.changes().get(0).isRecovery());
    }

    @Test
    void detectsAddedChecks() {
        HealthCheckSnapshot base = snapshot(List.of(result("db", "UP")));
        HealthCheckSnapshot target = snapshot(List.of(result("db", "UP"), result("cache", "UP")));
        var diff = comparator.compare(base, target);
        assertTrue(diff.addedChecks().contains("cache"));
        assertTrue(diff.removedChecks().isEmpty());
    }

    @Test
    void detectsRemovedChecks() {
        HealthCheckSnapshot base = snapshot(List.of(result("db", "UP"), result("cache", "UP")));
        HealthCheckSnapshot target = snapshot(List.of(result("db", "UP")));
        var diff = comparator.compare(base, target);
        assertTrue(diff.removedChecks().contains("cache"));
    }

    @Test
    void noChangesWhenIdentical() {
        HealthCheckSnapshot base = snapshot(List.of(result("db", "UP"), result("api", "UP")));
        HealthCheckSnapshot target = snapshot(List.of(result("db", "UP"), result("api", "UP")));
        var diff = comparator.compare(base, target);
        assertFalse(diff.hasChanges());
    }
}
