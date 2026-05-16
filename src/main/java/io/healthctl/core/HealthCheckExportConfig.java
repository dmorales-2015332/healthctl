package io.healthctl.core;

/**
 * Configuration for the HealthCheckExporter, specifying output format and destination.
 */
public class HealthCheckExportConfig {

    private final HealthCheckExporter.Format format;
    private final String outputPath;
    private final boolean appendMode;

    private HealthCheckExportConfig(Builder builder) {
        this.format = builder.format;
        this.outputPath = builder.outputPath;
        this.appendMode = builder.appendMode;
    }

    public HealthCheckExporter.Format getFormat() {
        return format;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public boolean isAppendMode() {
        return appendMode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private HealthCheckExporter.Format format = HealthCheckExporter.Format.TEXT;
        private String outputPath = "healthctl-export.txt";
        private boolean appendMode = false;

        public Builder format(HealthCheckExporter.Format format) {
            this.format = format;
            return this;
        }

        public Builder outputPath(String outputPath) {
            if (outputPath == null || outputPath.isBlank()) {
                throw new IllegalArgumentException("Output path must not be blank");
            }
            this.outputPath = outputPath;
            return this;
        }

        public Builder appendMode(boolean appendMode) {
            this.appendMode = appendMode;
            return this;
        }

        public HealthCheckExportConfig build() {
            return new HealthCheckExportConfig(this);
        }
    }

    @Override
    public String toString() {
        return "HealthCheckExportConfig{format=" + format +
               ", outputPath='" + outputPath + "', appendMode=" + appendMode + "}";
    }
}
