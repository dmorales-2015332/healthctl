package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthCheckGroupTest {

    private HealthCheckGroup group;

    @BeforeEach
    void setUp() {
        group = new HealthCheckGroup("db-checks", "Database health checks", true);
    }

    @Test
    void constructor_shouldSetFieldsCorrectly() {
        assertEquals("db-checks", group.getName());
        assertEquals("Database health checks", group.getDescription());
        assertTrue(group.isFailFast());
        assertTrue(group.isEmpty());
    }

    @Test
    void constructor_shouldDefaultFailFastToFalse() {
        HealthCheckGroup g = new HealthCheckGroup("net", "Network");
        assertFalse(g.isFailFast());
    }

    @Test
    void constructor_shouldThrowOnBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckGroup("", "desc"));
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckGroup(null, "desc"));
    }

    @Test
    void addCheck_shouldIncreaseSize() {
        HealthCheck check = mock(HealthCheck.class);
        group.addCheck(check);
        assertEquals(1, group.size());
        assertFalse(group.isEmpty());
    }

    @Test
    void addCheck_shouldThrowOnNull() {
        assertThrows(NullPointerException.class, () -> group.addCheck(null));
    }

    @Test
    void removeCheck_shouldDecreaseSize() {
        HealthCheck check = mock(HealthCheck.class);
        group.addCheck(check);
        assertTrue(group.removeCheck(check));
        assertEquals(0, group.size());
    }

    @Test
    void removeCheck_shouldReturnFalseWhenCheckNotPresent() {
        HealthCheck check = mock(HealthCheck.class);
        assertFalse(group.removeCheck(check));
        assertEquals(0, group.size());
    }

    @Test
    void getChecks_shouldReturnUnmodifiableList() {
        HealthCheck check = mock(HealthCheck.class);
        group.addCheck(check);
        assertThrows(UnsupportedOperationException.class, () -> group.getChecks().clear());
    }

    @Test
    void toString_shouldContainGroupName() {
        assertTrue(group.toString().contains("db-checks"));
    }
}
