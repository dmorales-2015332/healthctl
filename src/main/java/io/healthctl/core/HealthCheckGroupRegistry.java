package io.healthctl.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing named {@link HealthCheckGroup} instances.
 */
public class HealthCheckGroupRegistry {

    private final Map<String, HealthCheckGroup> groups = new ConcurrentHashMap<>();

    public void register(HealthCheckGroup group) {
        Objects.requireNonNull(group, "HealthCheckGroup must not be null");
        groups.put(group.getName(), group);
    }

    public boolean unregister(String groupName) {
        Objects.requireNonNull(groupName, "Group name must not be null");
        return groups.remove(groupName) != null;
    }

    public Optional<HealthCheckGroup> find(String groupName) {
        Objects.requireNonNull(groupName, "Group name must not be null");
        return Optional.ofNullable(groups.get(groupName));
    }

    public boolean contains(String groupName) {
        return groups.containsKey(groupName);
    }

    public Collection<HealthCheckGroup> getAll() {
        return Collections.unmodifiableCollection(groups.values());
    }

    public int size() {
        return groups.size();
    }

    public void clear() {
        groups.clear();
    }

    @Override
    public String toString() {
        return "HealthCheckGroupRegistry{groups=" + groups.keySet() + "}";
    }
}
