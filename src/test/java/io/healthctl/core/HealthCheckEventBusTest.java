package io.healthctl.core;

import io.healthctl.core.HealthCheckEventBus.EventType;
import io.healthctl.core.HealthCheckEventBus.HealthCheckEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckEventBusTest {

    private HealthCheckEventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new HealthCheckEventBus();
    }

    @Test
    void shouldDeliverEventToSubscriber() {
        List<HealthCheckEvent> received = new ArrayList<>();
        eventBus.subscribe(EventType.CHECK_PASSED, received::add);

        HealthCheckEvent event = new HealthCheckEvent(EventType.CHECK_PASSED, "db", null);
        eventBus.publish(event);

        assertEquals(1, received.size());
        assertEquals("db", received.get(0).getCheckName());
        assertEquals(EventType.CHECK_PASSED, received.get(0).getType());
    }

    @Test
    void shouldDeliverToMultipleSubscribersOfSameType() {
        List<String> log = new ArrayList<>();
        eventBus.subscribe(EventType.CHECK_FAILED, e -> log.add("handler1"));
        eventBus.subscribe(EventType.CHECK_FAILED, e -> log.add("handler2"));

        eventBus.publish(new HealthCheckEvent(EventType.CHECK_FAILED, "redis", null));

        assertEquals(2, log.size());
        assertTrue(log.contains("handler1"));
        assertTrue(log.contains("handler2"));
    }

    @Test
    void shouldNotDeliverToUnrelatedEventTypeSubscriber() {
        List<HealthCheckEvent> received = new ArrayList<>();
        eventBus.subscribe(EventType.CHECK_RECOVERED, received::add);

        eventBus.publish(new HealthCheckEvent(EventType.CHECK_FAILED, "api", null));

        assertTrue(received.isEmpty());
    }

    @Test
    void shouldIsolateFailingHandlerAndContinueDelivery() {
        List<String> log = new ArrayList<>();
        eventBus.subscribe(EventType.CHECK_DEGRADED, e -> { throw new RuntimeException("boom"); });
        eventBus.subscribe(EventType.CHECK_DEGRADED, e -> log.add("ok"));

        assertDoesNotThrow(() -> eventBus.publish(new HealthCheckEvent(EventType.CHECK_DEGRADED, "svc", null)));
        assertEquals(1, log.size());
    }

    @Test
    void shouldUnsubscribeAllHandlersForEventType() {
        List<HealthCheckEvent> received = new ArrayList<>();
        eventBus.subscribe(EventType.CHECK_STARTED, received::add);
        assertEquals(1, eventBus.subscriberCount(EventType.CHECK_STARTED));

        eventBus.unsubscribeAll(EventType.CHECK_STARTED);
        eventBus.publish(new HealthCheckEvent(EventType.CHECK_STARTED, "app", null));

        assertTrue(received.isEmpty());
        assertEquals(0, eventBus.subscriberCount(EventType.CHECK_STARTED));
    }

    @Test
    void shouldThrowOnNullSubscribeArguments() {
        assertThrows(IllegalArgumentException.class, () -> eventBus.subscribe(null, e -> {}));
        assertThrows(IllegalArgumentException.class, () -> eventBus.subscribe(EventType.CHECK_PASSED, null));
    }

    @Test
    void publishNullEventShouldBeNoOp() {
        assertDoesNotThrow(() -> eventBus.publish(null));
    }
}
