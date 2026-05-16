package io.healthctl.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Tracks consecutive failures per service and fires alerts based on configured policies.
 */
public class HealthCheckAlertManager {

    private final HealthCheckAlertPolicy defaultPolicy;
    private final Map<String, Integer> failureCounts = new ConcurrentHashMap<>();
    private final Map<String, Boolean> previouslyFailed = new ConcurrentHashMap<>();
    private final List<Consumer<HealthCheckAlert>> listeners = new ArrayList<>();

    public HealthCheckAlertManager(HealthCheckAlertPolicy defaultPolicy) {
        this.defaultPolicy = Objects.requireNonNull(defaultPolicy, "defaultPolicy must not be null");
    }

    public void addAlertListener(Consumer<HealthCheckAlert> listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    public void process(HealthCheckResult result) {
        String service = result.getServiceName();
        boolean failed = !result.isHealthy();

        if (failed) {
            int count = failureCounts.merge(service, 1, Integer::sum);
            HealthCheckAlert.Severity severity = defaultPolicy.evaluate(count);
            if (severity != null) {
                HealthCheckAlert alert = new HealthCheckAlert(
                        service, severity,
                        "Service unhealthy: " + result.getMessage(), count);
                notifyListeners(alert);
            }
            previouslyFailed.put(service, true);
        } else {
            boolean wasFailed = previouslyFailed.getOrDefault(service, false);
            failureCounts.put(service, 0);
            if (wasFailed && defaultPolicy.isAlertOnRecovery()) {
                HealthCheckAlert alert = new HealthCheckAlert(
                        service, HealthCheckAlert.Severity.WARNING,
                        "Service recovered: " + result.getMessage(), 0);
                notifyListeners(alert);
            }
            previouslyFailed.put(service, false);
        }
    }

    public int getFailureCount(String serviceName) {
        return failureCounts.getOrDefault(serviceName, 0);
    }

    private void notifyListeners(HealthCheckAlert alert) {
        for (Consumer<HealthCheckAlert> listener : listeners) {
            listener.accept(alert);
        }
    }
}
