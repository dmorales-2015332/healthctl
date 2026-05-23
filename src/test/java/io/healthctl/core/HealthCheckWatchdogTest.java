package io.healthctl.core;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckWatchdogTest {

    @Test
    void constructor_throwsOnNullThreshold() {
        assertThrows(IllegalArgumentException.class,
                () -> new HealthCheckWatchdog(null, name -> {}));
    }

    @Test
    void constructor_throwsOnZeroThreshold() {
        assertThrows(IllegalArgumentException.class,
                () -> new HealthCheckWatchdog(Duration.ZERO, name -> {}));
    }

    @Test
    void constructor_throwsOnNullCallback() {
        assertThrows(IllegalArgumentException.class,
                () -> new HealthCheckWatchdog(Duration.ofSeconds(5), null));
    }

    @Test
    void register_incrementsCount() {
        HealthCheckWatchdog watchdog = new HealthCheckWatchdog(Duration.ofSeconds(5), name -> {});
        assertEquals(0, watchdog.registeredCount());
        watchdog.register("db");
        assertEquals(1, watchdog.registeredCount());
        watchdog.register("cache");
        assertEquals(2, watchdog.registeredCount());
    }

    @Test
    void register_throwsOnBlankName() {
        HealthCheckWatchdog watchdog = new HealthCheckWatchdog(Duration.ofSeconds(5), name -> {});
        assertThrows(IllegalArgumentException.class, () -> watchdog.register(""));
        assertThrows(IllegalArgumentException.class, () -> watchdog.register("  "));
        assertThrows(IllegalArgumentException.class, () -> watchdog.register(null));
    }

    @Test
    void unregister_removesCheck() {
        HealthCheckWatchdog watchdog = new HealthCheckWatchdog(Duration.ofSeconds(5), name -> {});
        watchdog.register("svc");
        watchdog.unregister("svc");
        assertEquals(0, watchdog.registeredCount());
    }

    @Test
    void isStale_falseImmediatelyAfterHeartbeat() {
        HealthCheckWatchdog watchdog = new HealthCheckWatchdog(Duration.ofSeconds(10), name -> {});
        watchdog.register("api");
        watchdog.heartbeat("api");
        assertFalse(watchdog.isStale("api"));
    }

    @Test
    void isStale_trueAfterThresholdExceeded() throws InterruptedException {
        HealthCheckWatchdog watchdog = new HealthCheckWatchdog(Duration.ofMillis(50), name -> {});
        watchdog.register("slow-check");
        TimeUnit.MILLISECONDS.sleep(100);
        assertTrue(watchdog.isStale("slow-check"));
    }

    @Test
    void scanForStale_invokesCallbackForStaleChecks() throws InterruptedException {
        List<String> staleNames = new ArrayList<>();
        HealthCheckWatchdog watchdog = new HealthCheckWatchdog(Duration.ofMillis(50), staleNames::add);
        watchdog.register("stale-svc");
        watchdog.start(Duration.ofMillis(30));
        TimeUnit.MILLISECONDS.sleep(200);
        watchdog.stop();
        assertFalse(staleNames.isEmpty());
        assertTrue(staleNames.contains("stale-svc"));
    }

    @Test
    void heartbeat_preventsStaleDetection() throws InterruptedException {
        List<String> staleNames = new ArrayList<>();
        HealthCheckWatchdog watchdog = new HealthCheckWatchdog(Duration.ofMillis(80), staleNames::add);
        watchdog.register("live-svc");
        watchdog.start(Duration.ofMillis(30));
        for (int i = 0; i < 5; i++) {
            TimeUnit.MILLISECONDS.sleep(30);
            watchdog.heartbeat("live-svc");
        }
        watchdog.stop();
        assertTrue(staleNames.isEmpty());
    }
}
