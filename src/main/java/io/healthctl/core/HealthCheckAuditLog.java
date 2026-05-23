package io.healthctl.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Maintains an immutable audit trail of health check lifecycle events,
 * including state transitions, configuration changes, and manual overrides.
 */
public class HealthCheckAuditLog {

    public enum AuditEventType {
        CHECK_REGISTERED,
        CHECK_DEREGISTERED,
        CHECK_EXECUTED,
        STATUS_CHANGED,
        CONFIG_UPDATED,
        MANUAL_OVERRIDE,
        CIRCUIT_BREAKER_OPENED,
        CIRCUIT_BREAKER_CLOSED,
        ALERT_TRIGGERED
    }

    public static final class AuditEntry {
        private final Instant timestamp;
        private final String checkName;
        private final AuditEventType eventType;
        private final String detail;

        public AuditEntry(String checkName, AuditEventType eventType, String detail) {
            this.timestamp = Instant.now();
            this.checkName = Objects.requireNonNull(checkName, "checkName must not be null");
            this.eventType = Objects.requireNonNull(eventType, "eventType must not be null");
            this.detail = detail != null ? detail : "";
        }

        public Instant getTimestamp() { return timestamp; }
        public String getCheckName() { return checkName; }
        public AuditEventType getEventType() { return eventType; }
        public String getDetail() { return detail; }

        @Override
        public String toString() {
            return String.format("[%s] %s | %s | %s", timestamp, checkName, eventType, detail);
        }
    }

    private final int maxEntries;
    private final CopyOnWriteArrayList<AuditEntry> entries;

    public HealthCheckAuditLog(int maxEntries) {
        if (maxEntries <= 0) throw new IllegalArgumentException("maxEntries must be positive");
        this.maxEntries = maxEntries;
        this.entries = new CopyOnWriteArrayList<>();
    }

    public void record(String checkName, AuditEventType eventType, String detail) {
        AuditEntry entry = new AuditEntry(checkName, eventType, detail);
        entries.add(entry);
        while (entries.size() > maxEntries) {
            entries.remove(0);
        }
    }

    public List<AuditEntry> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public List<AuditEntry> getByCheckName(String checkName) {
        List<AuditEntry> result = new ArrayList<>();
        for (AuditEntry e : entries) {
            if (e.getCheckName().equals(checkName)) result.add(e);
        }
        return Collections.unmodifiableList(result);
    }

    public List<AuditEntry> getByEventType(AuditEventType type) {
        List<AuditEntry> result = new ArrayList<>();
        for (AuditEntry e : entries) {
            if (e.getEventType() == type) result.add(e);
        }
        return Collections.unmodifiableList(result);
    }

    public int size() {
        return entries.size();
    }

    public void clear() {
        entries.clear();
    }
}
