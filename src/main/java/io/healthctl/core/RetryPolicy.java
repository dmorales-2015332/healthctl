package io.healthctl.core;

/**
 * Configurable retry policy for health check executions.
 * Supports fixed delay and exponential backoff strategies.
 */
public class RetryPolicy {

    public enum Strategy {
        FIXED,
        EXPONENTIAL_BACKOFF
    }

    private final int maxAttempts;
    private final long initialDelayMs;
    private final long maxDelayMs;
    private final Strategy strategy;

    private RetryPolicy(Builder builder) {
        this.maxAttempts = builder.maxAttempts;
        this.initialDelayMs = builder.initialDelayMs;
        this.maxDelayMs = builder.maxDelayMs;
        this.strategy = builder.strategy;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long computeDelay(int attemptNumber) {
        if (strategy == Strategy.EXPONENTIAL_BACKOFF) {
            long delay = initialDelayMs * (1L << (attemptNumber - 1));
            return Math.min(delay, maxDelayMs);
        }
        return initialDelayMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int maxAttempts = 3;
        private long initialDelayMs = 1000L;
        private long maxDelayMs = 30_000L;
        private Strategy strategy = Strategy.FIXED;

        public Builder maxAttempts(int maxAttempts) {
            if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder initialDelayMs(long initialDelayMs) {
            if (initialDelayMs < 0) throw new IllegalArgumentException("initialDelayMs must be >= 0");
            this.initialDelayMs = initialDelayMs;
            return this;
        }

        public Builder maxDelayMs(long maxDelayMs) {
            this.maxDelayMs = maxDelayMs;
            return this;
        }

        public Builder strategy(Strategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public RetryPolicy build() {
            return new RetryPolicy(this);
        }
    }
}
