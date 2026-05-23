package io.healthctl.core;

import java.time.Duration;
import java.util.Objects;

/**
 * Defines the policy parameters for quarantining a health check
 * after repeated failures, including threshold and release duration.
 */
public class HealthCheckQuarantinePolicy {

    public static final int DEFAULT_FAILURE_THRESHOLD = 5;
    public static final Duration DEFAULT_QUARANTINE_DURATION = Duration.ofMinutes(2);

    private final int failureThreshold;
    private final Duration quarantineDuration;
    private final boolean autoRelease;

    private HealthCheckQuarantinePolicy(Builder builder) {
        this.failureThreshold = builder.failureThreshold;
        this.quarantineDuration = builder.quarantineDuration;
        this.autoRelease = builder.autoRelease;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public Duration getQuarantineDuration() {
        return quarantineDuration;
    }

    public boolean isAutoRelease() {
        return autoRelease;
    }

    public static HealthCheckQuarantinePolicy defaultPolicy() {
        return new Builder().build();
    }

    public HealthCheckQuarantineManager createManager() {
        return new HealthCheckQuarantineManager(failureThreshold, quarantineDuration);
    }

    @Override
    public String toString() {
        return "HealthCheckQuarantinePolicy{" +
                "failureThreshold=" + failureThreshold +
                ", quarantineDuration=" + quarantineDuration +
                ", autoRelease=" + autoRelease + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HealthCheckQuarantinePolicy)) return false;
        HealthCheckQuarantinePolicy that = (HealthCheckQuarantinePolicy) o;
        return failureThreshold == that.failureThreshold
                && autoRelease == that.autoRelease
                && Objects.equals(quarantineDuration, that.quarantineDuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(failureThreshold, quarantineDuration, autoRelease);
    }

    public static class Builder {
        private int failureThreshold = DEFAULT_FAILURE_THRESHOLD;
        private Duration quarantineDuration = DEFAULT_QUARANTINE_DURATION;
        private boolean autoRelease = true;

        public Builder failureThreshold(int threshold) {
            if (threshold <= 0) throw new IllegalArgumentException("threshold must be > 0");
            this.failureThreshold = threshold;
            return this;
        }

        public Builder quarantineDuration(Duration duration) {
            Objects.requireNonNull(duration, "duration must not be null");
            if (duration.isNegative() || duration.isZero()) {
                throw new IllegalArgumentException("duration must be positive");
            }
            this.quarantineDuration = duration;
            return this;
        }

        public Builder autoRelease(boolean autoRelease) {
            this.autoRelease = autoRelease;
            return this;
        }

        public HealthCheckQuarantinePolicy build() {
            return new HealthCheckQuarantinePolicy(this);
        }
    }
}
