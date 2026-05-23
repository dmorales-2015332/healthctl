package io.healthctl.core;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit breaker for health checks to prevent cascading failures.
 * Supports CLOSED, OPEN, and HALF_OPEN states.
 */
public class HealthCheckCircuitBreaker {

    public enum State { CLOSED, OPEN, HALF_OPEN }

    private final String serviceName;
    private final int failureThreshold;
    private final long resetTimeoutMillis;

    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private volatile Instant openedAt = null;

    public HealthCheckCircuitBreaker(String serviceName, int failureThreshold, long resetTimeoutMillis) {
        if (serviceName == null || serviceName.isBlank()) throw new IllegalArgumentException("serviceName must not be blank");
        if (failureThreshold <= 0) throw new IllegalArgumentException("failureThreshold must be positive");
        if (resetTimeoutMillis <= 0) throw new IllegalArgumentException("resetTimeoutMillis must be positive");
        this.serviceName = serviceName;
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMillis = resetTimeoutMillis;
    }

    public boolean allowRequest() {
        State current = state.get();
        if (current == State.CLOSED) return true;
        if (current == State.OPEN) {
            if (openedAt != null && Instant.now().toEpochMilli() - openedAt.toEpochMilli() >= resetTimeoutMillis) {
                state.compareAndSet(State.OPEN, State.HALF_OPEN);
                return true;
            }
            return false;
        }
        // HALF_OPEN: allow one probe request
        return true;
    }

    public void recordSuccess() {
        failureCount.set(0);
        state.set(State.CLOSED);
        openedAt = null;
    }

    public void recordFailure() {
        int failures = failureCount.incrementAndGet();
        if (state.get() == State.HALF_OPEN || failures >= failureThreshold) {
            state.set(State.OPEN);
            openedAt = Instant.now();
        }
    }

    public State getState() { return state.get(); }
    public int getFailureCount() { return failureCount.get(); }
    public String getServiceName() { return serviceName; }

    /**
     * Returns the remaining time in milliseconds before an OPEN circuit transitions
     * to HALF_OPEN, or 0 if the circuit is not currently OPEN or the timeout has
     * already elapsed.
     */
    public long getRemainingOpenMillis() {
        if (state.get() != State.OPEN || openedAt == null) {
            return 0L;
        }
        long elapsed = Instant.now().toEpochMilli() - openedAt.toEpochMilli();
        long remaining = resetTimeoutMillis - elapsed;
        return Math.max(0L, remaining);
    }

    public void reset() {
        failureCount.set(0);
        state.set(State.CLOSED);
        openedAt = null;
    }
}
