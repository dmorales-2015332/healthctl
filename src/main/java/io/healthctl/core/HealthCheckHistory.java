package io.healthctl.core;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maintains a rolling history of health check results for a given service.
 */
public class HealthCheckHistory {

    private final String serviceName;
    private final int maxEntries;
    private final Deque<HealthCheckResult> entries;

    public HealthCheckHistory(String serviceName, int maxEntries) {
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("serviceName must not be blank");
        }
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("maxEntries must be positive");
        }
        this.serviceName = serviceName;
        this.maxEntries = maxEntries;
        this.entries = new ArrayDeque<>();
    }

    public synchronized void record(HealthCheckResult result) {
        if (result == null) throw new IllegalArgumentException("result must not be null");
        if (entries.size() >= maxEntries) {
            entries.pollFirst();
        }
        entries.addLast(result);
    }

    public synchronized List<HealthCheckResult> getEntries() {
        return Collections.unmodifiableList(entries.stream().collect(Collectors.toList()));
    }

    public synchronized HealthCheckResult getLatest() {
        return entries.peekLast();
    }

    public synchronized long countByStatus(HealthCheckResult.Status status) {
        return entries.stream().filter(r -> r.getStatus() == status).count();
    }

    public synchronized double successRate() {
        if (entries.isEmpty()) return 0.0;
        long successes = countByStatus(HealthCheckResult.Status.HEALTHY);
        return (double) successes / entries.size();
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public synchronized int size() {
        return entries.size();
    }
}
