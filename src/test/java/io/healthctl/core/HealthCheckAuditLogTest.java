package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckAuditLogTest {

    private HealthCheckAuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new HealthCheckAuditLog(100);
    }

    @Test
    void shouldRecordAndRetrieveEntries() {
        auditLog.record("db-check", HealthCheckAuditLog.AuditEventType.CHECK_REGISTERED, "Registered database health check");
        auditLog.record("db-check", HealthCheckAuditLog.AuditEventType.CHECK_EXECUTED, "Exit code 0");

        List<HealthCheckAuditLog.AuditEntry> all = auditLog.getAll();
        assertEquals(2, all.size());
        assertEquals("db-check", all.get(0).getCheckName());
        assertEquals(HealthCheckAuditLog.AuditEventType.CHECK_REGISTERED, all.get(0).getEventType());
    }

    @Test
    void shouldFilterByCheckName() {
        auditLog.record("db-check", HealthCheckAuditLog.AuditEventType.CHECK_EXECUTED, "ok");
        auditLog.record("api-check", HealthCheckAuditLog.AuditEventType.CHECK_EXECUTED, "ok");
        auditLog.record("db-check", HealthCheckAuditLog.AuditEventType.STATUS_CHANGED, "UP -> DOWN");

        List<HealthCheckAuditLog.AuditEntry> dbEntries = auditLog.getByCheckName("db-check");
        assertEquals(2, dbEntries.size());
        dbEntries.forEach(e -> assertEquals("db-check", e.getCheckName()));
    }

    @Test
    void shouldFilterByEventType() {
        auditLog.record("svc-a", HealthCheckAuditLog.AuditEventType.ALERT_TRIGGERED, "threshold exceeded");
        auditLog.record("svc-b", HealthCheckAuditLog.AuditEventType.CHECK_EXECUTED, "exit 1");
        auditLog.record("svc-a", HealthCheckAuditLog.AuditEventType.ALERT_TRIGGERED, "repeated failure");

        List<HealthCheckAuditLog.AuditEntry> alerts = auditLog.getByEventType(HealthCheckAuditLog.AuditEventType.ALERT_TRIGGERED);
        assertEquals(2, alerts.size());
    }

    @Test
    void shouldRespectMaxEntriesCapacity() {
        HealthCheckAuditLog smallLog = new HealthCheckAuditLog(3);
        smallLog.record("svc", HealthCheckAuditLog.AuditEventType.CHECK_EXECUTED, "1");
        smallLog.record("svc", HealthCheckAuditLog.AuditEventType.CHECK_EXECUTED, "2");
        smallLog.record("svc", HealthCheckAuditLog.AuditEventType.CHECK_EXECUTED, "3");
        smallLog.record("svc", HealthCheckAuditLog.AuditEventType.CHECK_EXECUTED, "4");

        assertEquals(3, smallLog.size());
        assertEquals("4", smallLog.getAll().get(2).getDetail());
    }

    @Test
    void shouldClearAllEntries() {
        auditLog.record("x", HealthCheckAuditLog.AuditEventType.CHECK_REGISTERED, "");
        auditLog.clear();
        assertEquals(0, auditLog.size());
    }

    @Test
    void shouldRejectInvalidMaxEntries() {
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckAuditLog(0));
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckAuditLog(-5));
    }

    @Test
    void shouldIncludeTimestampInEntry() {
        auditLog.record("ts-check", HealthCheckAuditLog.AuditEventType.MANUAL_OVERRIDE, "forced UP");
        HealthCheckAuditLog.AuditEntry entry = auditLog.getAll().get(0);
        assertNotNull(entry.getTimestamp());
    }

    @Test
    void shouldReturnUnmodifiableList() {
        auditLog.record("immutable-check", HealthCheckAuditLog.AuditEventType.CHECK_EXECUTED, "ok");
        List<HealthCheckAuditLog.AuditEntry> all = auditLog.getAll();
        assertThrows(UnsupportedOperationException.class, () -> all.remove(0));
    }
}
