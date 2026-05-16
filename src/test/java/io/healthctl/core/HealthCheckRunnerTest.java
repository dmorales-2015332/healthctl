package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealthCheckRunnerTest {

    private RetryExecutor retryExecutor;
    private HealthCheckNotifier notifier;
    private HealthCheckRunner runner;

    @BeforeEach
    void setUp() {
        retryExecutor = mock(RetryExecutor.class);
        notifier = mock(HealthCheckNotifier.class);
        runner = new HealthCheckRunner(retryExecutor, notifier);
    }

    @Test
    void runAll_shouldReturnReportWithAllResults() {
        HealthCheck check1 = mock(HealthCheck.class);
        HealthCheck check2 = mock(HealthCheck.class);
        when(check1.getName()).thenReturn("db-check");
        when(check2.getName()).thenReturn("cache-check");

        HealthCheckResult result1 = new HealthCheckResult("db-check", true, "OK", 120);
        HealthCheckResult result2 = new HealthCheckResult("cache-check", true, "OK", 80);

        when(retryExecutor.execute(check1)).thenReturn(result1);
        when(retryExecutor.execute(check2)).thenReturn(result2);

        HealthCheckReport report = runner.runAll(List.of(check1, check2));

        assertNotNull(report);
        assertEquals(2, report.getResults().size());
        verify(notifier, never()).notify(any());
    }

    @Test
    void runAll_shouldNotifyOnFailedCheck() {
        HealthCheck check = mock(HealthCheck.class);
        when(check.getName()).thenReturn("api-check");

        HealthCheckResult failedResult = new HealthCheckResult("api-check", false, "Connection refused", 500);
        when(retryExecutor.execute(check)).thenReturn(failedResult);

        runner.runAll(List.of(check));

        verify(notifier, times(1)).notify(failedResult);
    }

    @Test
    void runAll_withEmptyList_shouldReturnEmptyReport() {
        HealthCheckReport report = runner.runAll(List.of());
        assertNotNull(report);
        assertTrue(report.getResults().isEmpty());
        verifyNoInteractions(retryExecutor, notifier);
    }

    @Test
    void runSingle_shouldReturnResultAndNotifyOnFailure() {
        HealthCheck check = mock(HealthCheck.class);
        when(check.getName()).thenReturn("disk-check");

        HealthCheckResult failedResult = new HealthCheckResult("disk-check", false, "Disk full", 300);
        when(retryExecutor.execute(check)).thenReturn(failedResult);

        HealthCheckResult result = runner.runSingle(check);

        assertFalse(result.isHealthy());
        verify(notifier, times(1)).notify(failedResult);
    }

    @Test
    void constructor_shouldThrowOnNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckRunner(null, notifier));
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckRunner(retryExecutor, null));
    }
}
