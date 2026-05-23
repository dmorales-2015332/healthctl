package io.healthctl.core;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a recurring maintenance window during which health checks
 * may be suppressed or treated with relaxed policies.
 */
public class HealthCheckMaintenanceWindow {

    private final String id;
    private final String description;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Set<DayOfWeek> activeDays;
    private final boolean suppressAlerts;

    public HealthCheckMaintenanceWindow(String id, String description,
                                        LocalTime startTime, LocalTime endTime,
                                        Set<DayOfWeek> activeDays, boolean suppressAlerts) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("startTime must not be after endTime");
        }
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.startTime = startTime;
        this.endTime = endTime;
        this.activeDays = activeDays == null || activeDays.isEmpty()
                ? EnumSet.allOf(DayOfWeek.class)
                : EnumSet.copyOf(activeDays);
        this.suppressAlerts = suppressAlerts;
    }

    public boolean isActive(LocalDateTime dateTime) {
        if (!activeDays.contains(dateTime.getDayOfWeek())) {
            return false;
        }
        LocalTime time = dateTime.toLocalTime();
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public Set<DayOfWeek> getActiveDays() { return EnumSet.copyOf(activeDays); }
    public boolean isSuppressAlerts() { return suppressAlerts; }

    @Override
    public String toString() {
        return "HealthCheckMaintenanceWindow{id='" + id + "', start=" + startTime +
                ", end=" + endTime + ", days=" + activeDays + ", suppressAlerts=" + suppressAlerts + "}";
    }
}
