package io.healthctl.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Notifies registered listeners when a health check changes state
 * (e.g., transitions from healthy to unhealthy or vice-versa).
 */
public class HealthCheckNotifier {

    private static final Logger LOGGER = Logger.getLogger(HealthCheckNotifier.class.getName());

    public enum Event {
        HEALTHY,
        UNHEALTHY,
        RECOVERED
    }

    public record Notification(
            String serviceName,
            Event event,
            HealthCheckResult result,
            Instant timestamp
    ) {}

    private final List<Consumer<Notification>> listeners = new ArrayList<>();
    private final java.util.Map<String, Boolean> previousStates = new java.util.concurrent.ConcurrentHashMap<>();

    public void addListener(Consumer<Notification> listener) {
        if (listener == null) throw new IllegalArgumentException("Listener must not be null");
        listeners.add(listener);
    }

    public void removeListener(Consumer<Notification> listener) {
        listeners.remove(listener);
    }

    /**
     * Evaluates the result and fires a notification if the service state has changed.
     *
     * @param serviceName the logical name of the service
     * @param result      the latest health check result
     */
    public void onResult(String serviceName, HealthCheckResult result) {
        if (serviceName == null || result == null) return;

        boolean currentlyHealthy = result.isSuccess();
        Boolean previouslyHealthy = previousStates.put(serviceName, currentlyHealthy);

        Event event;
        if (previouslyHealthy == null) {
            event = currentlyHealthy ? Event.HEALTHY : Event.UNHEALTHY;
        } else if (!previouslyHealthy && currentlyHealthy) {
            event = Event.RECOVERED;
        } else if (previouslyHealthy && !currentlyHealthy) {
            event = Event.UNHEALTHY;
        } else {
            return; // no state change
        }

        Notification notification = new Notification(serviceName, event, result, Instant.now());
        LOGGER.info(String.format("[%s] %s -> %s", serviceName, event, result.message()));
        listeners.forEach(l -> {
            try {
                l.accept(notification);
            } catch (Exception e) {
                LOGGER.warning("Listener threw exception: " + e.getMessage());
            }
        });
    }

    public int listenerCount() {
        return listeners.size();
    }
}
