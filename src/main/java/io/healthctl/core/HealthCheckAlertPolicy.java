package io.healthctl.core;

import java.util.Objects;

/**
 * Defines thresholds that determine when alerts should be raised for a service.
 */
public class HealthCheckAlertPolicy {

    private final int warningThreshold;
    private final int criticalThreshold;
    private final boolean alertOnRecovery;

    private HealthCheckAlertPolicy(Builder builder) {
        this.warningThreshold = builder.warningThreshold;
        this.criticalThreshold = builder.criticalThreshold;
        this.alertOnRecovery = builder.alertOnRecovery;
    }

    public int getWarningThreshold() {
        return warningThreshold;
    }

    public int getCriticalThreshold() {
        return criticalThreshold;
    }

    public boolean isAlertOnRecovery() {
        return alertOnRecovery;
    }

    public HealthCheckAlert.Severity evaluate(int consecutiveFailures) {
        if (consecutiveFailures >= criticalThreshold) {
            return HealthCheckAlert.Severity.CRITICAL;
        } else if (consecutiveFailures >= warningThreshold) {
            return HealthCheckAlert.Severity.WARNING;
        }
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int warningThreshold = 2;
        private int criticalThreshold = 5;
        private boolean alertOnRecovery = true;

        public Builder warningThreshold(int warningThreshold) {
            this.warningThreshold = warningThreshold;
            return this;
        }

        public Builder criticalThreshold(int criticalThreshold) {
            this.criticalThreshold = criticalThreshold;
            return this;
        }

        public Builder alertOnRecovery(boolean alertOnRecovery) {
            this.alertOnRecovery = alertOnRecovery;
            return this;
        }

        public HealthCheckAlertPolicy build() {
            Objects.requireNonNull(warningThreshold);
            if (warningThreshold >= criticalThreshold) {
                throw new IllegalArgumentException("warningThreshold must be less than criticalThreshold");
            }
            return new HealthCheckAlertPolicy(this);
        }
    }
}
