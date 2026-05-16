package io.healthctl.core;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents an alert triggered when a health check breaches a threshold.
 */
public class HealthCheckAlert {

    public enum Severity {
        WARNING, CRITICAL
    }

    private final String serviceName;
    private final Severity severity;
    private final String message;
    private final Instant triggeredAt;
    private final int consecutiveFailures;

    public HealthCheckAlert(String serviceName, Severity severity, String message, int consecutiveFailures) {
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName must not be null");
        this.severity = Objects.requireNonNull(severity, "severity must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
        this.consecutiveFailures = consecutiveFailures;
        this.triggeredAt = Instant.now();
    }

    public String getServiceName() {
        return serviceName;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTriggeredAt() {
        return triggeredAt;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s alert for '%s': %s (failures=%d)",
                triggeredAt, severity, serviceName, message, consecutiveFailures);
    }
}
