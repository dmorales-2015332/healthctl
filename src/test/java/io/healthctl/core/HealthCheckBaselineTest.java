package io.healthctl.core;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckBaselineTest {

    @Test
    void constructor_validArgs_createsBaseline() {
        HealthCheckBaseline b = new HealthCheckBaseline("svc-db", 0.95, 200L, Map.of("env", "prod"));
        assertEquals("svc-db", b.getCheckId());
        assertEquals(0.95, b.getExpectedSuccessRate());
        assertEquals(200L, b.getExpectedMaxLatencyMs());
        assertEquals("prod", b.getMetadata().get("env"));
        assertNotNull(b.getCapturedAt());
    }

    @Test
    void constructor_blankCheckId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new HealthCheckBaseline(" ", 0.9, 100L, null));
    }

    @Test
    void constructor_invalidSuccessRate_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new HealthCheckBaseline("svc", 1.5, 100L, null));
        assertThrows(IllegalArgumentException.class,
                () -> new HealthCheckBaseline("svc", -0.1, 100L, null));
    }

    @Test
    void constructor_negativeLatency_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new HealthCheckBaseline("svc", 0.9, -1L, null));
    }

    @Test
    void isLatencyWithinBaseline_returnsCorrectly() {
        HealthCheckBaseline b = new HealthCheckBaseline("svc", 0.9, 150L, null);
        assertTrue(b.isLatencyWithinBaseline(100L));
        assertTrue(b.isLatencyWithinBaseline(150L));
        assertFalse(b.isLatencyWithinBaseline(151L));
    }

    @Test
    void isSuccessRateWithinBaseline_returnsCorrectly() {
        HealthCheckBaseline b = new HealthCheckBaseline("svc", 0.9, 150L, null);
        assertTrue(b.isSuccessRateWithinBaseline(0.9));
        assertTrue(b.isSuccessRateWithinBaseline(1.0));
        assertFalse(b.isSuccessRateWithinBaseline(0.89));
    }

    @Test
    void nullMetadata_treatedAsEmpty() {
        HealthCheckBaseline b = new HealthCheckBaseline("svc", 0.8, 300L, null);
        assertNotNull(b.getMetadata());
        assertTrue(b.getMetadata().isEmpty());
    }

    @Test
    void equalsAndHashCode_basedOnKeyFields() {
        HealthCheckBaseline a = new HealthCheckBaseline("svc", 0.9, 200L, null);
        HealthCheckBaseline b = new HealthCheckBaseline("svc", 0.9, 200L, Map.of("k", "v"));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
