package io.healthctl.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Monitors health checks for staleness and triggers alerts when a check
 * has not reported a result within its expected interval.
 */
public class HealthCheckWatchdog {

    private final Duration stalenessThreshold;
    private final Consumer<String> onStale;
    private final Map<String, Instant> lastSeenMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> watchTask;

    public HealthCheckWatchdog(Duration stalenessThreshold, Consumer<String> onStale) {
        if (stalenessThreshold == null || stalenessThreshold.isNegative() || stalenessThreshold.isZero()) {
            throw new IllegalArgumentException("stalenessThreshold must be positive");
        }
        if (onStale == null) {
            throw new IllegalArgumentException("onStale callback must not be null");
        }
        this.stalenessThreshold = stalenessThreshold;
        this.onStale = onStale;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "healthctl-watchdog");
            t.setDaemon(true);
            return t;
        });
    }

    public void register(String checkName) {
        if (checkName == null || checkName.isBlank()) {
            throw new IllegalArgumentException("checkName must not be blank");
        }
        lastSeenMap.put(checkName, Instant.now());
    }

    public void heartbeat(String checkName) {
        lastSeenMap.computeIfPresent(checkName, (k, v) -> Instant.now());
    }

    public void unregister(String checkName) {
        lastSeenMap.remove(checkName);
    }

    public void start(Duration pollInterval) {
        long millis = pollInterval.toMillis();
        watchTask = scheduler.scheduleAtFixedRate(this::scanForStale, millis, millis, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (watchTask != null) {
            watchTask.cancel(false);
        }
        scheduler.shutdown();
    }

    private void scanForStale() {
        Instant now = Instant.now();
        lastSeenMap.forEach((name, lastSeen) -> {
            if (Duration.between(lastSeen, now).compareTo(stalenessThreshold) > 0) {
                onStale.accept(name);
            }
        });
    }

    public boolean isStale(String checkName) {
        Instant lastSeen = lastSeenMap.get(checkName);
        if (lastSeen == null) return false;
        return Duration.between(lastSeen, Instant.now()).compareTo(stalenessThreshold) > 0;
    }

    public int registeredCount() {
        return lastSeenMap.size();
    }
}
