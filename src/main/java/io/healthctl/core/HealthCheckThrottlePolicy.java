package io.healthctl.core;

import java.time.Duration;
import java.util.Objects;

/**
 * Defines a throttle policy for health checks, controlling minimum intervals
 * between consecutive executions to prevent resource exhaustion.
 */
public class HealthCheckThrottlePolicy {

    private final String checkId;
    private final Duration minInterval;
    private final int maxBurstCount;
    private final Duration burstWindow;
    private final boolean dropOnThrottle;

    private HealthCheckThrottlePolicy(Builder builder) {
        this.checkId = Objects.requireNonNull(builder.checkId, "checkId must not be null");
        this.minInterval = Objects.requireNonNull(builder.minInterval, "minInterval must not be null");
        this.maxBurstCount = builder.maxBurstCount;
        this.burstWindow = Objects.requireNonNull(builder.burstWindow, "burstWindow must not be null");
        this.dropOnThrottle = builder.dropOnThrottle;
    }

    public String getCheckId() { return checkId; }
    public Duration getMinInterval() { return minInterval; }
    public int getMaxBurstCount() { return maxBurstCount; }
    public Duration getBurstWindow() { return burstWindow; }
    public boolean isDropOnThrottle() { return dropOnThrottle; }

    public static Builder builder(String checkId) {
        return new Builder(checkId);
    }

    public static class Builder {
        private final String checkId;
        private Duration minInterval = Duration.ofSeconds(5);
        private int maxBurstCount = 3;
        private Duration burstWindow = Duration.ofSeconds(30);
        private boolean dropOnThrottle = false;

        private Builder(String checkId) {
            this.checkId = checkId;
        }

        public Builder minInterval(Duration minInterval) {
            this.minInterval = minInterval;
            return this;
        }

        public Builder maxBurstCount(int maxBurstCount) {
            if (maxBurstCount < 1) throw new IllegalArgumentException("maxBurstCount must be >= 1");
            this.maxBurstCount = maxBurstCount;
            return this;
        }

        public Builder burstWindow(Duration burstWindow) {
            this.burstWindow = burstWindow;
            return this;
        }

        public Builder dropOnThrottle(boolean dropOnThrottle) {
            this.dropOnThrottle = dropOnThrottle;
            return this;
        }

        public HealthCheckThrottlePolicy build() {
            return new HealthCheckThrottlePolicy(this);
        }
    }

    @Override
    public String toString() {
        return "HealthCheckThrottlePolicy{checkId='" + checkId + "', minInterval=" + minInterval +
               ", maxBurstCount=" + maxBurstCount + ", burstWindow=" + burstWindow +
               ", dropOnThrottle=" + dropOnThrottle + "}";
    }
}
