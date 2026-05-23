package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckQuarantineManagerTest {

    private HealthCheckQuarantineManager manager;

    @BeforeEach
    void setUp() {
        manager = new HealthCheckQuarantineManager(3, Duration.ofSeconds(10));
    }

    @Test
    void notQuarantinedInitially() {
        assertFalse(manager.isQuarantined("svc-a"));
    }

    @Test
    void quarantinedAfterThresholdFailures() {
        manager.recordFailure("svc-a");
        manager.recordFailure("svc-a");
        assertFalse(manager.isQuarantined("svc-a"));
        manager.recordFailure("svc-a");
        assertTrue(manager.isQuarantined("svc-a"));
    }

    @Test
    void successResetsQuarantine() {
        manager.recordFailure("svc-b");
        manager.recordFailure("svc-b");
        manager.recordFailure("svc-b");
        assertTrue(manager.isQuarantined("svc-b"));
        manager.recordSuccess("svc-b");
        assertFalse(manager.isQuarantined("svc-b"));
    }

    @Test
    void manualReleaseRemovesQuarantine() {
        manager.recordFailure("svc-c");
        manager.recordFailure("svc-c");
        manager.recordFailure("svc-c");
        assertTrue(manager.isQuarantined("svc-c"));
        manager.release("svc-c");
        assertFalse(manager.isQuarantined("svc-c"));
    }

    @Test
    void getQuarantineExpiryPresent() {
        manager.recordFailure("svc-d");
        manager.recordFailure("svc-d");
        manager.recordFailure("svc-d");
        assertTrue(manager.getQuarantineExpiry("svc-d").isPresent());
    }

    @Test
    void getQuarantineExpiryAbsentWhenNotQuarantined() {
        assertTrue(manager.getQuarantineExpiry("svc-e").isEmpty());
    }

    @Test
    void getQuarantinedChecksReturnsActiveEntries() {
        manager.recordFailure("svc-f");
        manager.recordFailure("svc-f");
        manager.recordFailure("svc-f");
        Set<String> quarantined = manager.getQuarantinedChecks();
        assertTrue(quarantined.contains("svc-f"));
    }

    @Test
    void failureCountTracked() {
        assertEquals(0, manager.getFailureCount("svc-g"));
        manager.recordFailure("svc-g");
        manager.recordFailure("svc-g");
        assertEquals(2, manager.getFailureCount("svc-g"));
    }

    @Test
    void invalidThresholdThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new HealthCheckQuarantineManager(0, Duration.ofSeconds(5)));
    }

    @Test
    void nullDurationThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new HealthCheckQuarantineManager(3, null));
    }
}
