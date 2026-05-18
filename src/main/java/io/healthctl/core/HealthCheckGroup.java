package io.healthctl.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a named group of health checks that can be executed and reported together.
 */
public class HealthCheckGroup {

    private final String name;
    private final String description;
    private final List<HealthCheck> checks;
    private final boolean failFast;

    public HealthCheckGroup(String name, String description, boolean failFast) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Group name must not be null or blank");
        }
        this.name = name;
        this.description = description != null ? description : "";
        this.failFast = failFast;
        this.checks = new ArrayList<>();
    }

    public HealthCheckGroup(String name, String description) {
        this(name, description, false);
    }

    public void addCheck(HealthCheck check) {
        Objects.requireNonNull(check, "HealthCheck must not be null");
        checks.add(check);
    }

    public boolean removeCheck(HealthCheck check) {
        return checks.remove(check);
    }

    public List<HealthCheck> getChecks() {
        return Collections.unmodifiableList(checks);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public int size() {
        return checks.size();
    }

    public boolean isEmpty() {
        return checks.isEmpty();
    }

    @Override
    public String toString() {
        return "HealthCheckGroup{name='" + name + "', checks=" + checks.size() + ", failFast=" + failFast + "}";
    }
}
