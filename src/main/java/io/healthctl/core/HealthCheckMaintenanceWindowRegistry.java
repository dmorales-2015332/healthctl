package io.healthctl.core;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing maintenance windows across health checks.
 * Provides lookup to determine whether a given check or the system
 * as a whole is currently within a maintenance window.
 */
public class HealthCheckMaintenanceWindowRegistry {

    private final Map<String, HealthCheckMaintenanceWindow> windows = new ConcurrentHashMap<>();
    // Maps checkId -> windowId for check-specific windows
    private final Map<String, String> checkWindowAssignments = new ConcurrentHashMap<>();

    public void register(HealthCheckMaintenanceWindow window) {
        if (window == null) throw new IllegalArgumentException("window must not be null");
        windows.put(window.getId(), window);
    }

    public void unregister(String windowId) {
        windows.remove(windowId);
        checkWindowAssignments.values().removeIf(wid -> wid.equals(windowId));
    }

    public void assignToCheck(String checkId, String windowId) {
        if (!windows.containsKey(windowId)) {
            throw new IllegalArgumentException("No window registered with id: " + windowId);
        }
        checkWindowAssignments.put(checkId, windowId);
    }

    public void unassignFromCheck(String checkId) {
        checkWindowAssignments.remove(checkId);
    }

    public Optional<HealthCheckMaintenanceWindow> getWindow(String windowId) {
        return Optional.ofNullable(windows.get(windowId));
    }

    public boolean isInMaintenanceWindow(String checkId, LocalDateTime dateTime) {
        String assignedWindowId = checkWindowAssignments.get(checkId);
        if (assignedWindowId != null) {
            HealthCheckMaintenanceWindow w = windows.get(assignedWindowId);
            if (w != null && w.isActive(dateTime)) return true;
        }
        // Also check any global windows (not assigned to specific checks)
        return windows.values().stream()
                .filter(w -> !checkWindowAssignments.containsValue(w.getId()))
                .anyMatch(w -> w.isActive(dateTime));
    }

    public boolean shouldSuppressAlerts(String checkId, LocalDateTime dateTime) {
        String assignedWindowId = checkWindowAssignments.get(checkId);
        if (assignedWindowId != null) {
            HealthCheckMaintenanceWindow w = windows.get(assignedWindowId);
            if (w != null && w.isActive(dateTime) && w.isSuppressAlerts()) return true;
        }
        return windows.values().stream()
                .filter(w -> !checkWindowAssignments.containsValue(w.getId()))
                .anyMatch(w -> w.isActive(dateTime) && w.isSuppressAlerts());
    }

    public Collection<HealthCheckMaintenanceWindow> getAllWindows() {
        return Collections.unmodifiableCollection(windows.values());
    }

    public int size() {
        return windows.size();
    }
}
