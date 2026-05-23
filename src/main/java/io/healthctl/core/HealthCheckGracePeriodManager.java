package io.healthctl.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages grace periods for health checks, suppressing alerts and failures
 * during a defined startup or maintenance window after a check is registered.
 */
public class HealthCheckGracePeriodManager {

    private final Map<String, GracePeriodEntry> gracePeriods = new ConcurrentHashMap<>();

    /**
     * Registers a grace period for the given health check name.
     *
     * @param checkName the name of the health check
     * @param duration  the duration of the grace period
     */
    public void register(String checkName, Duration duration) {
        if (checkName == null || checkName.isBlank()) {
            throw new IllegalArgumentException("checkName must not be null or blank");
        }
        if (duration == null || duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("duration must be a positive value");
        }
        Instant expiry = Instant.now().plus(duration);
        gracePeriods.put(checkName, new GracePeriodEntry(checkName, expiry));
    }

    /**
     * Returns true if the given health check is currently within its grace period.
     *
     * @param checkName the name of the health check
     * @return true if still in grace period, false otherwise
     */
    public boolean isInGracePeriod(String checkName) {
        GracePeriodEntry entry = gracePeriods.get(checkName);
        if (entry == null) {
            return false;
        }
        if (Instant.now().isBefore(entry.expiry())) {
            return true;
        }
        gracePeriods.remove(checkName);
        return false;
    }

    /**
     * Removes the grace period for the given health check, if any.
     *
     * @param checkName the name of the health check
     */
    public void cancel(String checkName) {
        gracePeriods.remove(checkName);
    }

    /**
     * Returns the expiry instant of the grace period for the given check,
     * or null if no active grace period exists.
     *
     * @param checkName the name of the health check
     * @return expiry instant or null
     */
    public Instant getExpiry(String checkName) {
        GracePeriodEntry entry = gracePeriods.get(checkName);
        if (entry == null) {
            return null;
        }
        return entry.expiry();
    }

    /** Returns the number of currently tracked (possibly expired) grace periods. */
    public int size() {
        return gracePeriods.size();
    }

    private record GracePeriodEntry(String checkName, Instant expiry) {}
}
