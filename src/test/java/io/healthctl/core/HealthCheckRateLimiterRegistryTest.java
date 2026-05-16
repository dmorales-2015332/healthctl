package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckRateLimiterRegistryTest {

    private HealthCheckRateLimiterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new HealthCheckRateLimiterRegistry(5, 60_000L);
    }

    @Test
    void shouldCreateDefaultLimiterForUnknownService() {
        HealthCheckRateLimiter limiter = registry.getOrCreate("newService");
        assertNotNull(limiter);
    }

    @Test
    void shouldReturnSameLimiterInstanceForSameService() {
        HealthCheckRateLimiter first = registry.getOrCreate("svc");
        HealthCheckRateLimiter second = registry.getOrCreate("svc");
        assertSame(first, second);
    }

    @Test
    void shouldRegisterCustomLimiter() {
        HealthCheckRateLimiter custom = new HealthCheckRateLimiter(2, 30_000L);
        registry.register("customSvc", custom);
        assertSame(custom, registry.getOrCreate("customSvc"));
    }

    @Test
    void shouldDelegateAcquireToUnderlyingLimiter() {
        registry = new HealthCheckRateLimiterRegistry(1, 60_000L);
        assertTrue(registry.tryAcquire("limitedSvc"));
        assertFalse(registry.tryAcquire("limitedSvc"));
    }

    @Test
    void shouldReportRegisteredService() {
        registry.getOrCreate("existingSvc");
        assertTrue(registry.isRegistered("existingSvc"));
        assertFalse(registry.isRegistered("missingSvc"));
    }

    @Test
    void shouldDeregisterService() {
        registry.getOrCreate("toRemove");
        registry.deregister("toRemove");
        assertFalse(registry.isRegistered("toRemove"));
    }

    @Test
    void shouldThrowOnNullServiceNameInRegister() {
        assertThrows(IllegalArgumentException.class,
                () -> registry.register(null, new HealthCheckRateLimiter(1, 1000)));
    }
}
