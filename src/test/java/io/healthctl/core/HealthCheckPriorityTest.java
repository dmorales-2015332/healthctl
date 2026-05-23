package io.healthctl.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckPriorityTest {

    @Test
    void testConstructorAndGetters() {
        HealthCheckPriority p = new HealthCheckPriority("db-check", HealthCheckPriority.Level.HIGH, 5);
        assertEquals("db-check", p.getCheckName());
        assertEquals(HealthCheckPriority.Level.HIGH, p.getLevel());
        assertEquals(5, p.getWeight());
    }

    @Test
    void testDefaultWeightFromLevel() {
        HealthCheckPriority p = new HealthCheckPriority("api-check", HealthCheckPriority.Level.CRITICAL);
        assertEquals(HealthCheckPriority.Level.CRITICAL.getOrder(), p.getWeight());
    }

    @Test
    void testCompareToOrdersByLevel() {
        HealthCheckPriority critical = new HealthCheckPriority("a", HealthCheckPriority.Level.CRITICAL);
        HealthCheckPriority low = new HealthCheckPriority("b", HealthCheckPriority.Level.LOW);
        assertTrue(critical.compareTo(low) < 0);
        assertTrue(low.compareTo(critical) > 0);
    }

    @Test
    void testCompareToEqualLevelUsesWeight() {
        HealthCheckPriority p1 = new HealthCheckPriority("a", HealthCheckPriority.Level.MEDIUM, 1);
        HealthCheckPriority p2 = new HealthCheckPriority("b", HealthCheckPriority.Level.MEDIUM, 3);
        assertTrue(p1.compareTo(p2) < 0);
    }

    @Test
    void testEqualsAndHashCode() {
        HealthCheckPriority p1 = new HealthCheckPriority("svc", HealthCheckPriority.Level.HIGH, 2);
        HealthCheckPriority p2 = new HealthCheckPriority("svc", HealthCheckPriority.Level.HIGH, 2);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void testInvalidCheckNameThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new HealthCheckPriority("  ", HealthCheckPriority.Level.LOW));
        assertThrows(IllegalArgumentException.class, () ->
            new HealthCheckPriority(null, HealthCheckPriority.Level.LOW));
    }

    @Test
    void testNullLevelThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new HealthCheckPriority("check", null));
    }

    @Test
    void testNegativeWeightThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new HealthCheckPriority("check", HealthCheckPriority.Level.LOW, -1));
    }

    @Test
    void testToString() {
        HealthCheckPriority p = new HealthCheckPriority("my-check", HealthCheckPriority.Level.MEDIUM, 2);
        String str = p.toString();
        assertTrue(str.contains("my-check"));
        assertTrue(str.contains("MEDIUM"));
    }
}
