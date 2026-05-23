package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckMaintenanceWindowTest {

    private HealthCheckMaintenanceWindow window;
    private HealthCheckMaintenanceWindowRegistry registry;

    @BeforeEach
    void setUp() {
        window = new HealthCheckMaintenanceWindow(
                "mw-1", "Nightly maintenance",
                LocalTime.of(2, 0), LocalTime.of(4, 0),
                EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                true
        );
        registry = new HealthCheckMaintenanceWindowRegistry();
    }

    @Test
    void windowIsActiveWithinTimeRange() {
        LocalDateTime saturday3am = LocalDateTime.of(2024, 1, 6, 3, 0); // Saturday
        assertTrue(window.isActive(saturday3am));
    }

    @Test
    void windowIsInactiveOutsideTimeRange() {
        LocalDateTime saturday10am = LocalDateTime.of(2024, 1, 6, 10, 0);
        assertFalse(window.isActive(saturday10am));
    }

    @Test
    void windowIsInactiveOnWrongDay() {
        LocalDateTime monday3am = LocalDateTime.of(2024, 1, 8, 3, 0); // Monday
        assertFalse(window.isActive(monday3am));
    }

    @Test
    void windowWithNoDaysDefaultsToAllDays() {
        HealthCheckMaintenanceWindow allDays = new HealthCheckMaintenanceWindow(
                "mw-all", "All days", LocalTime.of(1, 0), LocalTime.of(2, 0), null, false);
        LocalDateTime monday1am = LocalDateTime.of(2024, 1, 8, 1, 30);
        assertTrue(allDays.isActive(monday1am));
    }

    @Test
    void invalidTimeRangeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new HealthCheckMaintenanceWindow("bad", "bad",
                        LocalTime.of(5, 0), LocalTime.of(2, 0), null, false));
    }

    @Test
    void registryDetectsActiveWindowForCheck() {
        registry.register(window);
        registry.assignToCheck("db-check", "mw-1");
        LocalDateTime saturday3am = LocalDateTime.of(2024, 1, 6, 3, 0);
        assertTrue(registry.isInMaintenanceWindow("db-check", saturday3am));
    }

    @Test
    void registryReturnsFalseWhenNoWindowActive() {
        registry.register(window);
        registry.assignToCheck("db-check", "mw-1");
        LocalDateTime monday10am = LocalDateTime.of(2024, 1, 8, 10, 0);
        assertFalse(registry.isInMaintenanceWindow("db-check", monday10am));
    }

    @Test
    void registrySuppressAlertsWhenWindowActive() {
        registry.register(window);
        registry.assignToCheck("api-check", "mw-1");
        LocalDateTime saturday3am = LocalDateTime.of(2024, 1, 6, 3, 0);
        assertTrue(registry.shouldSuppressAlerts("api-check", saturday3am));
    }

    @Test
    void registryUnregisterRemovesWindow() {
        registry.register(window);
        registry.assignToCheck("db-check", "mw-1");
        registry.unregister("mw-1");
        assertEquals(0, registry.size());
        LocalDateTime saturday3am = LocalDateTime.of(2024, 1, 6, 3, 0);
        assertFalse(registry.isInMaintenanceWindow("db-check", saturday3am));
    }
}
