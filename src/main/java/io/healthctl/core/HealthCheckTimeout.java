package io.healthctl.core;

import java.time.Duration;
import java.util.Objects;

/**
 * Represents a timeout configuration for a health check execution.
 * Encapsulates timeout duration and behavior on timeout.
 */
public class HealthCheckTimeout {

    private final Duration duration;
    private final boolean failOnTimeout;
    private final String timeoutMessage;

    private HealthCheckTimeout(Builder builder) {
        this.duration = Objects.requireNonNull(builder.duration, "duration must not be null");
        this.failOnTimeout = builder.failOnTimeout;
        this.timeoutMessage = builder.timeoutMessage != null
                ? builder.timeoutMessage
                : "Health check timed out after " + duration.toMillis() + "ms";
    }

    public Duration getDuration() {
        return duration;
    }

    public boolean isFailOnTimeout() {
        return failOnTimeout;
    }

    public String getTimeoutMessage() {
        return timeoutMessage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static HealthCheckTimeout of(Duration duration) {
        return builder().duration(duration).build();
    }

    public static class Builder {
        private Duration duration;
        private boolean failOnTimeout = true;
        private String timeoutMessage;

        public Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public Builder failOnTimeout(boolean failOnTimeout) {
            this.failOnTimeout = failOnTimeout;
            return this;
        }

        public Builder timeoutMessage(String timeoutMessage) {
            this.timeoutMessage = timeoutMessage;
            return this;
        }

        public HealthCheckTimeout build() {
            return new HealthCheckTimeout(this);
        }
    }

    @Override
    public String toString() {
        return "HealthCheckTimeout{duration=" + duration + ", failOnTimeout=" + failOnTimeout + "}";
    }
}
