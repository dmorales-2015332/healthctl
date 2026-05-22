package io.healthctl.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Simple in-process event bus for publishing and subscribing to health check lifecycle events.
 */
public class HealthCheckEventBus {

    public enum EventType {
        CHECK_STARTED,
        CHECK_PASSED,
        CHECK_FAILED,
        CHECK_RECOVERED,
        CHECK_DEGRADED
    }

    public static class HealthCheckEvent {
        private final EventType type;
        private final String checkName;
        private final HealthCheckResult result;
        private final long timestamp;

        public HealthCheckEvent(EventType type, String checkName, HealthCheckResult result) {
            this.type = type;
            this.checkName = checkName;
            this.result = result;
            this.timestamp = System.currentTimeMillis();
        }

        public EventType getType() { return type; }
        public String getCheckName() { return checkName; }
        public HealthCheckResult getResult() { return result; }
        public long getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return "HealthCheckEvent{type=" + type + ", checkName='" + checkName + "', timestamp=" + timestamp + "}";
        }
    }

    private final Map<EventType, List<Consumer<HealthCheckEvent>>> subscribers = new ConcurrentHashMap<>();

    public void subscribe(EventType eventType, Consumer<HealthCheckEvent> handler) {
        if (eventType == null || handler == null) {
            throw new IllegalArgumentException("EventType and handler must not be null");
        }
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    }

    public void publish(HealthCheckEvent event) {
        if (event == null) return;
        List<Consumer<HealthCheckEvent>> handlers = subscribers.get(event.getType());
        if (handlers != null) {
            for (Consumer<HealthCheckEvent> handler : handlers) {
                try {
                    handler.accept(event);
                } catch (Exception e) {
                    // Isolate handler failures to avoid disrupting other subscribers
                    System.err.println("[HealthCheckEventBus] Handler error for event " + event + ": " + e.getMessage());
                }
            }
        }
    }

    public void unsubscribeAll(EventType eventType) {
        subscribers.remove(eventType);
    }

    public int subscriberCount(EventType eventType) {
        List<Consumer<HealthCheckEvent>> handlers = subscribers.get(eventType);
        return handlers == null ? 0 : handlers.size();
    }
}
