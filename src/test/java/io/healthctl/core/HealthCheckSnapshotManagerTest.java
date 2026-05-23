package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckSnapshotManagerTest {

    private HealthCheckSnapshotManager manager;

    private HealthCheckResult result(String name, String status) {
        return new HealthCheckResult(name, status, 100L, Instant.now(), "");
    }

    @BeforeEach
    void setUp() {
        manager = new HealthCheckSnapshotManager(3);
    }

    @Test
    void captureStoresSnapshot() {
        HealthCheckSnapshot snap = manager.capture(List.of(result("db", "UP")), Map.of());
        assertNotNull(snap.getSnapshotId());
        assertEquals(1, manager.size());
    }

    @Test
    void getByIdReturnsCorrectSnapshot() {
        HealthCheckSnapshot snap = manager.capture(List.of(result("db", "UP")), Map.of());
        Optional<HealthCheckSnapshot> found = manager.getById(snap.getSnapshotId());
        assertTrue(found.isPresent());
        assertEquals(snap.getSnapshotId(), found.get().getSnapshotId());
    }

    @Test
    void getLatestReturnsLastCaptured() {
        manager.capture(List.of(result("db", "UP")), Map.of());
        HealthCheckSnapshot last = manager.capture(List.of(result("cache", "DOWN")), Map.of());
        Optional<HealthCheckSnapshot> latest = manager.getLatest();
        assertTrue(latest.isPresent());
        assertEquals(last.getSnapshotId(), latest.get().getSnapshotId());
    }

    @Test
    void evictsOldestWhenMaxExceeded() {
        HealthCheckSnapshot first = manager.capture(List.of(result("a", "UP")), Map.of());
        manager.capture(List.of(result("b", "UP")), Map.of());
        manager.capture(List.of(result("c", "UP")), Map.of());
        manager.capture(List.of(result("d", "UP")), Map.of());
        assertEquals(3, manager.size());
        assertTrue(manager.getById(first.getSnapshotId()).isEmpty());
    }

    @Test
    void clearRemovesAllSnapshots() {
        manager.capture(List.of(result("x", "UP")), Map.of());
        manager.clear();
        assertEquals(0, manager.size());
        assertTrue(manager.getLatest().isEmpty());
    }

    @Test
    void constructorRejectsInvalidMax() {
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckSnapshotManager(0));
    }
}
