package io.healthctl.core;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Configuration for a single health check, including the command to run,
 * retry policy, and optional tags for grouping.
 */
public class HealthCheckConfig {

    private final String name;
    private final String command;
    private final RetryPolicy retryPolicy;
    private final List<String> tags;
    private final int timeoutSeconds;

    private HealthCheckConfig(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "name must not be null");
        this.command = Objects.requireNonNull(builder.command, "command must not be null");
        this.retryPolicy = Objects.requireNonNull(builder.retryPolicy, "retryPolicy must not be null");
        this.tags = Collections.unmodifiableList(builder.tags);
        this.timeoutSeconds = builder.timeoutSeconds;
    }

    public String getName() {
        return name;
    }

    public String getCommand() {
        return command;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public List<String> getTags() {
        return tags;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public static Builder builder(String name, String command) {
        return new Builder(name, command);
    }

    public static class Builder {
        private final String name;
        private final String command;
        private RetryPolicy retryPolicy = RetryPolicy.noRetry();
        private List<String> tags = Collections.emptyList();
        private int timeoutSeconds = 30;

        private Builder(String name, String command) {
            this.name = name;
            this.command = command;
        }

        public Builder retryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags != null ? tags : Collections.emptyList();
            return this;
        }

        public Builder timeoutSeconds(int timeoutSeconds) {
            if (timeoutSeconds <= 0) throw new IllegalArgumentException("timeoutSeconds must be positive");
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public HealthCheckConfig build() {
            return new HealthCheckConfig(this);
        }
    }
}
