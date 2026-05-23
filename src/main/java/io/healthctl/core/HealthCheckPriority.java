package io.healthctl.core;

import java.util.Objects;

/**
 * Represents a priority level for a health check, used to order execution
 * and determine urgency of alerts.
 */
public class HealthCheckPriority implements Comparable<HealthCheckPriority> {

    public enum Level {
        CRITICAL(0),
        HIGH(1),
        MEDIUM(2),
        LOW(3);

        private final int order;

        Level(int order) {
            this.order = order;
        }

        public int getOrder() {
            return order;
        }
    }

    private final String checkName;
    private final Level level;
    private final int weight;

    public HealthCheckPriority(String checkName, Level level, int weight) {
        if (checkName == null || checkName.isBlank()) {
            throw new IllegalArgumentException("checkName must not be null or blank");
        }
        if (level == null) {
            throw new IllegalArgumentException("level must not be null");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("weight must be non-negative");
        }
        this.checkName = checkName;
        this.level = level;
        this.weight = weight;
    }

    public HealthCheckPriority(String checkName, Level level) {
        this(checkName, level, level.getOrder());
    }

    public String getCheckName() {
        return checkName;
    }

    public Level getLevel() {
        return level;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public int compareTo(HealthCheckPriority other) {
        int levelCmp = Integer.compare(this.level.getOrder(), other.level.getOrder());
        if (levelCmp != 0) return levelCmp;
        return Integer.compare(this.weight, other.weight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HealthCheckPriority)) return false;
        HealthCheckPriority that = (HealthCheckPriority) o;
        return weight == that.weight &&
               Objects.equals(checkName, that.checkName) &&
               level == that.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(checkName, level, weight);
    }

    @Override
    public String toString() {
        return "HealthCheckPriority{checkName='" + checkName + "', level=" + level + ", weight=" + weight + "}";
    }
}
