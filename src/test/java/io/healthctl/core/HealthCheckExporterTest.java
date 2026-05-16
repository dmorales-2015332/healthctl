package io.healthctl.core;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckExporterTest {

    private HealthCheckResult makeResult(String name, String status, String message) {
        return new HealthCheckResult(name, status, message, LocalDateTime.of(2024, 6, 1, 12, 0, 0));
    }

    @Test
    void testExportJson_singleResult() throws IOException {
        HealthCheckExporter exporter = new HealthCheckExporter(HealthCheckExporter.Format.JSON);
        StringWriter writer = new StringWriter();
        HealthCheckResult result = makeResult("db", "UP", "Connected");
        exporter.export(List.of(result), writer);
        String output = writer.toString();
        assertTrue(output.contains("\"name\": \"db\""));
        assertTrue(output.contains("\"status\": \"UP\""));
        assertTrue(output.contains("\"message\": \"Connected\""));
    }

    @Test
    void testExportText_singleResult() throws IOException {
        HealthCheckExporter exporter = new HealthCheckExporter(HealthCheckExporter.Format.TEXT);
        StringWriter writer = new StringWriter();
        HealthCheckResult result = makeResult("cache", "DOWN", "Timeout");
        exporter.export(List.of(result), writer);
        String output = writer.toString();
        assertTrue(output.contains("cache"));
        assertTrue(output.contains("DOWN"));
        assertTrue(output.contains("Timeout"));
    }

    @Test
    void testExportJson_multipleResults() throws IOException {
        HealthCheckExporter exporter = new HealthCheckExporter(HealthCheckExporter.Format.JSON);
        StringWriter writer = new StringWriter();
        List<HealthCheckResult> results = List.of(
            makeResult("svc-a", "UP", "OK"),
            makeResult("svc-b", "DOWN", "Error")
        );
        exporter.export(results, writer);
        String output = writer.toString();
        assertTrue(output.contains("svc-a"));
        assertTrue(output.contains("svc-b"));
        assertTrue(output.startsWith("["));
        assertTrue(output.trim().endsWith("]"));
    }

    @Test
    void testExportText_emptyList() throws IOException {
        HealthCheckExporter exporter = new HealthCheckExporter(HealthCheckExporter.Format.TEXT);
        StringWriter writer = new StringWriter();
        exporter.export(List.of(), writer);
        assertEquals("", writer.toString());
    }

    @Test
    void testNullFormatThrows() {
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckExporter(null));
    }

    @Test
    void testNullResultsThrows() {
        HealthCheckExporter exporter = new HealthCheckExporter(HealthCheckExporter.Format.TEXT);
        assertThrows(IllegalArgumentException.class, () -> exporter.export(null, new StringWriter()));
    }

    @Test
    void testGetFormat() {
        HealthCheckExporter exporter = new HealthCheckExporter(HealthCheckExporter.Format.JSON);
        assertEquals(HealthCheckExporter.Format.JSON, exporter.getFormat());
    }
}
