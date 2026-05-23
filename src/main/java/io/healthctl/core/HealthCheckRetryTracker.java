package io.healthctl.core;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks retry attempt counts and timing metadata for health checks.
 * Integrates with RetryPolicy to provide observability into retry behavior.
 */
public class HealthCheckRetryTracker {

    private final Map<String, AtomicInteger> attemptCounts = new ConcurrentHashMap<>();
    private final Map<String, Instant> firstAttemptTimes = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastAttemptTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> maxAttemptsConfig = new ConcurrentHashMap<>();

    public void register(String checkId, int maxAttempts) {
        attemptCounts.putIfAbsent(checkId, new AtomicInteger(0));
        maxAttemptsConfig.put(checkId, maxAttempts);
    }

    public int recordAttempt(String checkId) {
        Instant now = Instant.now();
        firstAttemptTimes.putIfAbsent(checkId, now);
        lastAttemptTimes.put(checkId, now);
        AtomicInteger counter = attemptCounts.computeIfAbsent(checkId, k -> new AtomicInteger(0));
        return counter.incrementAndGet();
    }

    public int getAttemptCount(String checkId) {
        AtomicInteger counter = attemptCounts.get(checkId);
        return counter != null ? counter.get() : 0;
    }

    public boolean isExhausted(String checkId) {
        int attempts = getAttemptCount(checkId);
        Integer max = maxAttemptsConfig.get(checkId);
        return max != null && attempts >= max;
    }

    public Instant getFirstAttemptTime(String checkId) {
        return firstAttemptTimes.get(checkId);
    }

    public Instant getLastAttemptTime(String checkId) {
        return lastAttemptTimes.get(checkId);
    }

    public long getElapsedMillis(String checkId) {
        Instant first = firstAttemptTimes.get(checkId);
        Instant last = lastAttemptTimes.get(checkId);
        if (first == null || last == null) return 0L;
        return last.toEpochMilli() - first.toEpochMilli();
    }

    public void reset(String checkId) {
        attemptCounts.computeIfPresent(checkId, (k, v) -> { v.set(0); return v; });
        firstAttemptTimes.remove(checkId);
        lastAttemptTimes.remove(checkId);
    }

    public void unregister(String checkId) {
        attemptCounts.remove(checkId);
        firstAttemptTimes.remove(checkId);
        lastAttemptTimes.remove(checkId);
        maxAttemptsConfig.remove(checkId);
    }
}
