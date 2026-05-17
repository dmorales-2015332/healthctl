package io.healthctl.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a tag that can be applied to a health check for grouping and filtering.
 */
public class HealthCheckTag {

    private final String name;
    private final String description;
    private final Set<String> attributes;

    private HealthCheckTag(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "Tag name must not be null");
        this.description = builder.description != null ? builder.description : "";
        this.attributes = Collections.unmodifiableSet(new HashSet<>(builder.attributes));
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getAttributes() {
        return attributes;
    }

    public boolean hasAttribute(String attribute) {
        return attributes.contains(attribute);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HealthCheckTag)) return false;
        HealthCheckTag that = (HealthCheckTag) o;
        return name.equalsIgnoreCase(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }

    @Override
    public String toString() {
        return "HealthCheckTag{name='" + name + "', description='" + description + "'}";
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private String description;
        private final Set<String> attributes = new HashSet<>();

        private Builder(String name) {
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder attribute(String attribute) {
            this.attributes.add(attribute);
            return this;
        }

        public HealthCheckTag build() {
            return new HealthCheckTag(this);
        }
    }
}
