package io.healthctl.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry that stores per-service {@link HealthCheckHistory} instances.
 */
public class HealthCheckHistoryStore {

    private static final int DEFAULT_MAX_ENTRIES = 100;

    private final int maxEntriesPerService;
    private final Map<String, HealthCheckHistory> store = new ConcurrentHashMap<>();

    public HealthCheckHistoryStore() {
        this(DEFAULT_MAX_ENTRIES);
    }

    public HealthCheckHistoryStore(int maxEntriesPerService) {
        if (maxEntriesPerService <= 0) {
            throw new IllegalArgumentException("maxEntriesPerService must be positive");
        }
        this.maxEntriesPerService = maxEntriesPerService;
    }

    public void record(String serviceName, HealthCheckResult result) {
        store.computeIfAbsent(serviceName,
                name -> new HealthCheckHistory(name, maxEntriesPerService))
             .record(result);
    }

    public Optional<HealthCheckHistory> getHistory(String serviceName) {
        return Optional.ofNullable(store.get(serviceName));
    }

    public Collection<HealthCheckHistory> getAllHistories() {
        return Collections.unmodifiableCollection(store.values());
    }

    public void clear(String serviceName) {
        store.remove(serviceName);
    }

    public void clearAll() {
        store.clear();
    }

    public int getMaxEntriesPerService() {
        return maxEntriesPerService;
    }
}
