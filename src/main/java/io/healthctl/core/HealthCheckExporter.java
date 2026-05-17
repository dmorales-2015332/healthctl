package io.healthctl.core;

import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports health check results to various output formats (JSON, plain text).
 */
public class HealthCheckExporter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public enum Format {
        JSON, TEXT
    }

    private final Format format;

    public HealthCheckExporter(Format format) {
        if (format == null) throw new IllegalArgumentException("Format must not be null");
        this.format = format;
    }

    public void export(List<HealthCheckResult> results, Writer writer) throws IOException {
        if (results == null) throw new IllegalArgumentException("Results must not be null");
        if (writer == null) throw new IllegalArgumentException("Writer must not be null");

        if (format == Format.JSON) {
            exportJson(results, writer);
        } else {
            exportText(results, writer);
        }
        writer.flush();
    }

    private void exportJson(List<HealthCheckResult> results, Writer writer) throws IOException {
        writer.write("[\n");
        for (int i = 0; i < results.size(); i++) {
            HealthCheckResult r = results.get(i);
            writer.write("  {\n");
            writer.write("    \"name\": \"" + escape(r.getName()) + "\",\n");
            writer.write("    \"status\": \"" + r.getStatus() + "\",\n");
            writer.write("    \"message\": \"" + escape(r.getMessage()) + "\",\n");
            writer.write("    \"timestamp\": \"" + r.getTimestamp().format(FORMATTER) + "\"\n");
            writer.write("  }" + (i < results.size() - 1 ? "," : "") + "\n");
        }
        writer.write("]\n");
    }

    private void exportText(List<HealthCheckResult> results, Writer writer) throws IOException {
        for (HealthCheckResult r : results) {
            writer.write(String.format("[%s] %s | %s | %s%n",
                r.getTimestamp().format(FORMATTER),
                r.getName(),
                r.getStatus(),
                r.getMessage()));
        }
    }

    /**
     * Exports only results matching the given status to the writer.
     *
     * @param results  the full list of health check results
     * @param status   the status to filter by
     * @param writer   the output writer
     * @throws IOException if writing fails
     */
    public void exportFiltered(List<HealthCheckResult> results, HealthCheckResult.Status status, Writer writer) throws IOException {
        if (results == null) throw new IllegalArgumentException("Results must not be null");
        if (status == null) throw new IllegalArgumentException("Status must not be null");
        if (writer == null) throw new IllegalArgumentException("Writer must not be null");

        List<HealthCheckResult> filtered = results.stream()
                .filter(r -> status.equals(r.getStatus()))
                .toList();
        export(filtered, writer);
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public Format getFormat() {
        return format;
    }
}
