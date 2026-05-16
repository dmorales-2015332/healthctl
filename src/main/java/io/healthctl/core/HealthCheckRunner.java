package io.healthctl.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orchestrates execution of multiple health checks using configured retry policies.
 * Collects results and builds a consolidated HealthCheckReport.
 */
public class HealthCheckRunner {

    private static final Logger LOGGER = Logger.getLogger(HealthCheckRunner.class.getName());

    private final RetryExecutor retryExecutor;
    private final HealthCheckNotifier notifier;

    public HealthCheckRunner(RetryExecutor retryExecutor, HealthCheckNotifier notifier) {
        if (retryExecutor == null) throw new IllegalArgumentException("retryExecutor must not be null");
        if (notifier == null) throw new IllegalArgumentException("notifier must not be null");
        this.retryExecutor = retryExecutor;
        this.notifier = notifier;
    }

    /**
     * Runs all provided health checks and returns a consolidated report.
     *
     * @param checks list of health checks to execute
     * @return HealthCheckReport containing all results
     */
    public HealthCheckReport runAll(List<HealthCheck> checks) {
        if (checks == null || checks.isEmpty()) {
            LOGGER.warning("No health checks provided to runner.");
            return new HealthCheckReport(List.of(), Instant.now());
        }

        List<HealthCheckResult> results = new ArrayList<>();

        for (HealthCheck check : checks) {
            LOGGER.info("Running health check: " + check.getName());
            HealthCheckResult result = retryExecutor.execute(check);
            results.add(result);

            if (!result.isHealthy()) {
                LOGGER.log(Level.WARNING, "Health check failed: {0} — {1}",
                        new Object[]{check.getName(), result.getMessage()});
                notifier.notify(result);
            } else {
                LOGGER.info("Health check passed: " + check.getName());
            }
        }

        return new HealthCheckReport(results, Instant.now());
    }

    /**
     * Runs a single health check and returns its result.
     *
     * @param check the health check to execute
     * @return HealthCheckResult for the given check
     */
    public HealthCheckResult runSingle(HealthCheck check) {
        if (check == null) throw new IllegalArgumentException("check must not be null");
        LOGGER.info("Running single health check: " + check.getName());
        HealthCheckResult result = retryExecutor.execute(check);
        if (!result.isHealthy()) {
            notifier.notify(result);
        }
        return result;
    }
}
