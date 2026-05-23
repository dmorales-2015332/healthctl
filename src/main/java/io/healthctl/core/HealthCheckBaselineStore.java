package io.healthctl.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe store for persisting and retrieving health check baselines.
 */
public class HealthCheckBaselineStore {

    private final Map<String, HealthCheckBaseline> baselines = new ConcurrentHashMap<>();

    public void save(HealthCheckBaseline baseline) {
        if (baseline == null) {
            throw new IllegalArgumentException("baseline must not be null");
        }
        baselines.put(baseline.getCheckId(), baseline);
    }

    public Optional<HealthCheckBaseline> findById(String checkId) {
        if (checkId == null || checkId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(baselines.get(checkId));
    }

    public boolean remove(String checkId) {
        if (checkId == null) return false;
        return baselines.remove(checkId) != null;
    }

    public Collection<HealthCheckBaseline> findAll() {
        return Collections.unmodifiableCollection(baselines.values());
    }

    public boolean contains(String checkId) {
        return checkId != null && baselines.containsKey(checkId);
    }

    public int size() {
        return baselines.size();
    }

    public void clear() {
        baselines.clear();
    }
}
