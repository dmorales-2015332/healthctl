package io.healthctl.core;

import java.time.Instant;

/**
 * Represents the outcome of a single health check execution.
 */
public class HealthCheckResult {

    public enum Status {
        HEALTHY,
        UNHEALTHY,
        UNKNOWN
    }

    private final String serviceName;
    private final Status status;
    private final int exitCode;
    private final String output;
    private final Instant timestamp;
    private final long durationMillis;

    public HealthCheckResult(String serviceName, Status status, int exitCode,
                             String output, Instant timestamp, long durationMillis) {
        this.serviceName = serviceName;
        this.status = status;
        this.exitCode = exitCode;
        this.output = output;
        this.timestamp = timestamp;
        this.durationMillis = durationMillis;
    }

    public String getServiceName() { return serviceName; }
    public Status getStatus() { return status; }
    public int getExitCode() { return exitCode; }
    public String getOutput() { return output; }
    public Instant getTimestamp() { return timestamp; }
    public long getDurationMillis() { return durationMillis; }

    public boolean isHealthy() {
        return status == Status.HEALTHY;
    }

    @Override
    public String toString() {
        return String.format("HealthCheckResult{service='%s', status=%s, exitCode=%d, duration=%dms, timestamp=%s}",
                serviceName, status, exitCode, durationMillis, timestamp);
    }
}
