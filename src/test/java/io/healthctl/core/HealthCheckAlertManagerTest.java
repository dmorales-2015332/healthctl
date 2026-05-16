package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckAlertManagerTest {

    private HealthCheckAlertPolicy policy;
    private HealthCheckAlertManager manager;
    private List<HealthCheckAlert> capturedAlerts;

    @BeforeEach
    void setUp() {
        policy = HealthCheckAlertPolicy.builder()
                .warningThreshold(2)
                .criticalThreshold(4)
                .alertOnRecovery(true)
                .build();
        manager = new HealthCheckAlertManager(policy);
        capturedAlerts = new ArrayList<>();
        manager.addAlertListener(capturedAlerts::add);
    }

    private HealthCheckResult failure(String service) {
        return new HealthCheckResult(service, false, "connection refused");
    }

    private HealthCheckResult success(String service) {
        return new HealthCheckResult(service, true, "OK");
    }

    @Test
    void shouldNotAlertBelowWarningThreshold() {
        manager.process(failure("svc-a"));
        assertTrue(capturedAlerts.isEmpty());
        assertEquals(1, manager.getFailureCount("svc-a"));
    }

    @Test
    void shouldRaiseWarningAtThreshold() {
        manager.process(failure("svc-a"));
        manager.process(failure("svc-a"));
        assertEquals(1, capturedAlerts.size());
        assertEquals(HealthCheckAlert.Severity.WARNING, capturedAlerts.get(0).getSeverity());
    }

    @Test
    void shouldRaiseCriticalAtCriticalThreshold() {
        for (int i = 0; i < 4; i++) {
            manager.process(failure("svc-b"));
        }
        long criticals = capturedAlerts.stream()
                .filter(a -> a.getSeverity() == HealthCheckAlert.Severity.CRITICAL)
                .count();
        assertTrue(criticals >= 1);
    }

    @Test
    void shouldResetFailureCountOnSuccess() {
        manager.process(failure("svc-c"));
        manager.process(failure("svc-c"));
        manager.process(success("svc-c"));
        assertEquals(0, manager.getFailureCount("svc-c"));
    }

    @Test
    void shouldAlertOnRecovery() {
        manager.process(failure("svc-d"));
        manager.process(failure("svc-d"));
        int alertsBefore = capturedAlerts.size();
        manager.process(success("svc-d"));
        assertEquals(alertsBefore + 1, capturedAlerts.size());
        HealthCheckAlert recoveryAlert = capturedAlerts.get(capturedAlerts.size() - 1);
        assertTrue(recoveryAlert.getMessage().contains("recovered"));
    }

    @Test
    void shouldNotAlertOnRecoveryIfPolicyDisabled() {
        HealthCheckAlertPolicy noRecoveryPolicy = HealthCheckAlertPolicy.builder()
                .warningThreshold(2)
                .criticalThreshold(4)
                .alertOnRecovery(false)
                .build();
        HealthCheckAlertManager noRecoveryManager = new HealthCheckAlertManager(noRecoveryPolicy);
        List<HealthCheckAlert> alerts = new ArrayList<>();
        noRecoveryManager.addAlertListener(alerts::add);
        noRecoveryManager.process(failure("svc-e"));
        noRecoveryManager.process(failure("svc-e"));
        int countAfterFailures = alerts.size();
        noRecoveryManager.process(success("svc-e"));
        assertEquals(countAfterFailures, alerts.size());
    }
}
