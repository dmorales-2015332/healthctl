package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckNotifierTest {

    private HealthCheckNotifier notifier;
    private List<HealthCheckNotifier.Notification> received;

    @BeforeEach
    void setUp() {
        notifier = new HealthCheckNotifier();
        received = new ArrayList<>();
        notifier.addListener(received::add);
    }

    @Test
    void shouldFireHealthyEventOnFirstSuccessfulResult() {
        notifier.onResult("svc-a", HealthCheckResult.success("ok"));
        assertEquals(1, received.size());
        assertEquals(HealthCheckNotifier.Event.HEALTHY, received.get(0).event());
    }

    @Test
    void shouldFireUnhealthyEventOnFirstFailedResult() {
        notifier.onResult("svc-a", HealthCheckResult.failure("timeout"));
        assertEquals(1, received.size());
        assertEquals(HealthCheckNotifier.Event.UNHEALTHY, received.get(0).event());
    }

    @Test
    void shouldNotFireEventWhenStateUnchanged() {
        notifier.onResult("svc-a", HealthCheckResult.success("ok"));
        notifier.onResult("svc-a", HealthCheckResult.success("still ok"));
        assertEquals(1, received.size());
    }

    @Test
    void shouldFireRecoveredEventWhenServiceComesBackUp() {
        notifier.onResult("svc-a", HealthCheckResult.failure("down"));
        notifier.onResult("svc-a", HealthCheckResult.success("back up"));
        assertEquals(2, received.size());
        assertEquals(HealthCheckNotifier.Event.RECOVERED, received.get(1).event());
    }

    @Test
    void shouldFireUnhealthyAfterPreviouslyHealthy() {
        notifier.onResult("svc-b", HealthCheckResult.success("ok"));
        notifier.onResult("svc-b", HealthCheckResult.failure("crashed"));
        assertEquals(2, received.size());
        assertEquals(HealthCheckNotifier.Event.UNHEALTHY, received.get(1).event());
    }

    @Test
    void shouldTrackMultipleServicesIndependently() {
        notifier.onResult("svc-a", HealthCheckResult.success("ok"));
        notifier.onResult("svc-b", HealthCheckResult.failure("err"));
        assertEquals(2, received.size());
        assertEquals("svc-a", received.get(0).serviceName());
        assertEquals("svc-b", received.get(1).serviceName());
    }

    @Test
    void shouldReflectCorrectListenerCount() {
        assertEquals(1, notifier.listenerCount());
        notifier.addListener(n -> {});
        assertEquals(2, notifier.listenerCount());
    }

    @Test
    void shouldIgnoreNullInputsGracefully() {
        assertDoesNotThrow(() -> notifier.onResult(null, HealthCheckResult.success("ok")));
        assertDoesNotThrow(() -> notifier.onResult("svc", null));
        assertTrue(received.isEmpty());
    }
}
