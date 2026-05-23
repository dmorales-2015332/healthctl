package io.healthctl.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages creation, storage, and retrieval of health check snapshots.
 * Retains up to a configurable maximum number of snapshots (FIFO eviction).
 */
public class HealthCheckSnapshotManager {

    private final int maxSnapshots;
    private final LinkedHashMap<String, HealthCheckSnapshot> store;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public HealthCheckSnapshotManager(int maxSnapshots) {
        if (maxSnapshots <= 0) throw new IllegalArgumentException("maxSnapshots must be > 0");
        this.maxSnapshots = maxSnapshots;
        this.store = new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, HealthCheckSnapshot> eldest) {
                return size() > maxSnapshots;
            }
        };
    }

    public HealthCheckSnapshot capture(List<HealthCheckResult> results, Map<String, String> metadata) {
        String id = UUID.randomUUID().toString();
        HealthCheckSnapshot snapshot = new HealthCheckSnapshot(id, Instant.now(), results, metadata);
        lock.writeLock().lock();
        try {
            store.put(id, snapshot);
        } finally {
            lock.writeLock().unlock();
        }
        return snapshot;
    }

    public Optional<HealthCheckSnapshot> getById(String snapshotId) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(store.get(snapshotId));
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<HealthCheckSnapshot> getAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(store.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<HealthCheckSnapshot> getLatest() {
        lock.readLock().lock();
        try {
            return store.values().stream().reduce((first, second) -> second);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return store.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            store.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
