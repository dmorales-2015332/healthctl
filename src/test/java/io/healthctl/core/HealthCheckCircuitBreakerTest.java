package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckCircuitBreakerTest {

    private HealthCheckCircuitBreaker breaker;

    @BeforeEach
    void setUp() {
        breaker = new HealthCheckCircuitBreaker("test-service", 3, 500L);
    }

    @Test
    void initialStateIsClosed() {
        assertEquals(HealthCheckCircuitBreaker.State.CLOSED, breaker.getState());
        assertTrue(breaker.allowRequest());
    }

    @Test
    void opensAfterFailureThreshold() {
        breaker.recordFailure();
        breaker.recordFailure();
        assertEquals(HealthCheckCircuitBreaker.State.CLOSED, breaker.getState());
        breaker.recordFailure();
        assertEquals(HealthCheckCircuitBreaker.State.OPEN, breaker.getState());
        assertFalse(breaker.allowRequest());
    }

    @Test
    void successResetsClosed() {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordSuccess();
        assertEquals(HealthCheckCircuitBreaker.State.CLOSED, breaker.getState());
        assertEquals(0, breaker.getFailureCount());
        assertTrue(breaker.allowRequest());
    }

    @Test
    void transitionsToHalfOpenAfterTimeout() throws InterruptedException {
        HealthCheckCircuitBreaker fastBreaker = new HealthCheckCircuitBreaker("fast", 1, 100L);
        fastBreaker.recordFailure();
        assertEquals(HealthCheckCircuitBreaker.State.OPEN, fastBreaker.getState());
        Thread.sleep(150);
        assertTrue(fastBreaker.allowRequest());
        assertEquals(HealthCheckCircuitBreaker.State.HALF_OPEN, fastBreaker.getState());
    }

    @Test
    void halfOpenFailureReopensCircuit() throws InterruptedException {
        HealthCheckCircuitBreaker fastBreaker = new HealthCheckCircuitBreaker("fast", 1, 100L);
        fastBreaker.recordFailure();
        Thread.sleep(150);
        fastBreaker.allowRequest(); // transitions to HALF_OPEN
        fastBreaker.recordFailure();
        assertEquals(HealthCheckCircuitBreaker.State.OPEN, fastBreaker.getState());
    }

    @Test
    void resetRestoresClosedState() {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        assertEquals(HealthCheckCircuitBreaker.State.OPEN, breaker.getState());
        breaker.reset();
        assertEquals(HealthCheckCircuitBreaker.State.CLOSED, breaker.getState());
        assertEquals(0, breaker.getFailureCount());
    }

    @Test
    void registryCreatesAndReusesBreakers() {
        HealthCheckCircuitBreakerRegistry registry = new HealthCheckCircuitBreakerRegistry(2, 200L);
        HealthCheckCircuitBreaker b1 = registry.getOrCreate("svc-a");
        HealthCheckCircuitBreaker b2 = registry.getOrCreate("svc-a");
        assertSame(b1, b2);
        assertEquals(1, registry.size());
    }

    @Test
    void invalidArgumentsThrow() {
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckCircuitBreaker("", 3, 500));
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckCircuitBreaker("svc", 0, 500));
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckCircuitBreaker("svc", 3, 0));
    }
}
