package io.healthctl.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckExportConfigTest {

    @Test
    void testDefaultValues() {
        HealthCheckExportConfig config = HealthCheckExportConfig.builder().build();
        assertEquals(HealthCheckExporter.Format.TEXT, config.getFormat());
        assertEquals("healthctl-export.txt", config.getOutputPath());
        assertFalse(config.isAppendMode());
    }

    @Test
    void testCustomValues() {
        HealthCheckExportConfig config = HealthCheckExportConfig.builder()
            .format(HealthCheckExporter.Format.JSON)
            .outputPath("/var/log/healthctl.json")
            .appendMode(true)
            .build();
        assertEquals(HealthCheckExporter.Format.JSON, config.getFormat());
        assertEquals("/var/log/healthctl.json", config.getOutputPath());
        assertTrue(config.isAppendMode());
    }

    @Test
    void testBlankOutputPathThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            HealthCheckExportConfig.builder().outputPath("  ").build());
    }

    @Test
    void testNullOutputPathThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            HealthCheckExportConfig.builder().outputPath(null).build());
    }

    @Test
    void testToStringContainsFields() {
        HealthCheckExportConfig config = HealthCheckExportConfig.builder()
            .format(HealthCheckExporter.Format.JSON)
            .outputPath("out.json")
            .build();
        String str = config.toString();
        assertTrue(str.contains("JSON"));
        assertTrue(str.contains("out.json"));
    }
}
