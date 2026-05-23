package io.healthctl.core;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages throttling of health check executions based on configured throttle policies.
 * Tracks last-execution timestamps and burst counts per check ID.
 */
public class HealthCheckThrottleManager {

    private final Map<String, HealthCheckThrottlePolicy> policies = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastExecutionTimes = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> burstWindows = new ConcurrentHashMap<>();
    private final Clock clock;

    public HealthCheckThrottleManager() {
        this(Clock.systemUTC());
    }

    public HealthCheckThrottleManager(Clock clock) {
        this.clock = clock;
    }

    public void registerPolicy(HealthCheckThrottlePolicy policy) {
        policies.put(policy.getCheckId(), policy);
        burstWindows.putIfAbsent(policy.getCheckId(), new ArrayDeque<>());
    }

    public void deregisterPolicy(String checkId) {
        policies.remove(checkId);
        lastExecutionTimes.remove(checkId);
        burstWindows.remove(checkId);
    }

    public Optional<HealthCheckThrottlePolicy> getPolicy(String checkId) {
        return Optional.ofNullable(policies.get(checkId));
    }

    /**
     * Returns true if the given check is allowed to execute now.
     * Records the execution timestamp if allowed.
     */
    public synchronized boolean tryAcquire(String checkId) {
        HealthCheckThrottlePolicy policy = policies.get(checkId);
        if (policy == null) return true;

        Instant now = clock.instant();
        Instant lastExec = lastExecutionTimes.get(checkId);

        if (lastExec != null) {
            long elapsedMs = now.toEpochMilli() - lastExec.toEpochMilli();
            if (elapsedMs < policy.getMinInterval().toMillis()) {
                return false;
            }
        }

        Deque<Instant> window = burstWindows.computeIfAbsent(checkId, k -> new ArrayDeque<>());
        Instant windowStart = now.minus(policy.getBurstWindow());
        while (!window.isEmpty() && window.peekFirst().isBefore(windowStart)) {
            window.pollFirst();
        }

        if (window.size() >= policy.getMaxBurstCount()) {
            return false;
        }

        window.addLast(now);
        lastExecutionTimes.put(checkId, now);
        return true;
    }

    public void recordExecution(String checkId) {
        lastExecutionTimes.put(checkId, clock.instant());
    }

    public int registeredPolicyCount() {
        return policies.size();
    }
}
