package io.healthctl.core;

import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Executes a health check command with a configurable timeout.
 * Wraps command execution in a Future and cancels if duration is exceeded.
 */
public class HealthCheckTimeoutExecutor {

    private static final Logger logger = Logger.getLogger(HealthCheckTimeoutExecutor.class.getName());

    private final ExecutorService executor;

    public HealthCheckTimeoutExecutor() {
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "healthctl-timeout-worker");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Executes the given callable within the timeout defined by the policy.
     *
     * @param task    the health check task to execute
     * @param timeout the timeout configuration
     * @return HealthCheckResult reflecting success, failure, or timeout
     */
    public HealthCheckResult execute(Callable<HealthCheckResult> task, HealthCheckTimeout timeout) {
        Future<HealthCheckResult> future = executor.submit(task);
        try {
            return future.get(timeout.getDuration().toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            logger.warning("Health check timed out: " + timeout.getTimeoutMessage());
            if (timeout.isFailOnTimeout()) {
                return HealthCheckResult.failure(timeout.getTimeoutMessage());
            }
            return HealthCheckResult.unknown(timeout.getTimeoutMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return HealthCheckResult.failure("Health check interrupted");
        } catch (ExecutionException e) {
            return HealthCheckResult.failure("Health check execution error: " + e.getCause().getMessage());
        }
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
