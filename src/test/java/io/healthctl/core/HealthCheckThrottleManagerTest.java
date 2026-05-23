package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckThrottleManagerTest {

    private AtomicLong epochMillis;
    private Clock testClock;
    private HealthCheckThrottleManager manager;

    @BeforeEach
    void setUp() {
        epochMillis = new AtomicLong(Instant.parse("2024-01-01T00:00:00Z").toEpochMilli());
        testClock = Clock.fixed(Instant.ofEpochMilli(epochMillis.get()), ZoneOffset.UTC);
        manager = new HealthCheckThrottleManager(testClock);
    }

    private void advanceTime(Duration duration) {
        epochMillis.addAndGet(duration.toMillis());
        testClock = Clock.fixed(Instant.ofEpochMilli(epochMillis.get()), ZoneOffset.UTC);
        manager = new HealthCheckThrottleManager(testClock);
    }

    @Test
    void allowsExecutionWithNoPolicy() {
        assertTrue(manager.tryAcquire("unknown-check"));
    }

    @Test
    void allowsFirstExecutionWithPolicy() {
        HealthCheckThrottlePolicy policy = HealthCheckThrottlePolicy.builder("svc-a")
                .minInterval(Duration.ofSeconds(10))
                .maxBurstCount(3)
                .burstWindow(Duration.ofMinutes(1))
                .build();
        manager.registerPolicy(policy);
        assertTrue(manager.tryAcquire("svc-a"));
    }

    @Test
    void throttlesWhenMinIntervalNotElapsed() {
        HealthCheckThrottlePolicy policy = HealthCheckThrottlePolicy.builder("svc-b")
                .minInterval(Duration.ofSeconds(10))
                .maxBurstCount(5)
                .burstWindow(Duration.ofMinutes(1))
                .build();
        manager.registerPolicy(policy);
        assertTrue(manager.tryAcquire("svc-b"));
        assertFalse(manager.tryAcquire("svc-b"), "Should be throttled before minInterval elapses");
    }

    @Test
    void throttlesOnBurstExceeded() {
        HealthCheckThrottlePolicy policy = HealthCheckThrottlePolicy.builder("svc-c")
                .minInterval(Duration.ofMillis(1))
                .maxBurstCount(2)
                .burstWindow(Duration.ofMinutes(5))
                .build();
        manager.registerPolicy(policy);
        assertTrue(manager.tryAcquire("svc-c"));
        assertTrue(manager.tryAcquire("svc-c"));
        assertFalse(manager.tryAcquire("svc-c"), "Burst limit exceeded");
    }

    @Test
    void deregisterRemovesPolicy() {
        HealthCheckThrottlePolicy policy = HealthCheckThrottlePolicy.builder("svc-d")
                .minInterval(Duration.ofSeconds(5))
                .build();
        manager.registerPolicy(policy);
        assertEquals(1, manager.registeredPolicyCount());
        manager.deregisterPolicy("svc-d");
        assertEquals(0, manager.registeredPolicyCount());
        assertTrue(manager.tryAcquire("svc-d"), "No policy means always allowed");
    }

    @Test
    void policyBuilderDefaultsAreReasonable() {
        HealthCheckThrottlePolicy policy = HealthCheckThrottlePolicy.builder("svc-e").build();
        assertEquals("svc-e", policy.getCheckId());
        assertEquals(Duration.ofSeconds(5), policy.getMinInterval());
        assertEquals(3, policy.getMaxBurstCount());
        assertFalse(policy.isDropOnThrottle());
    }

    @Test
    void getPolicyReturnsRegisteredPolicy() {
        HealthCheckThrottlePolicy policy = HealthCheckThrottlePolicy.builder("svc-f").build();
        manager.registerPolicy(policy);
        assertTrue(manager.getPolicy("svc-f").isPresent());
        assertFalse(manager.getPolicy("nonexistent").isPresent());
    }
}
