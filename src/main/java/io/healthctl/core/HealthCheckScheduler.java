package io.healthctl.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Schedules and runs health checks at a fixed interval, collecting results.
 */
public class HealthCheckScheduler implements AutoCloseable {

    private static final Logger log = Logger.getLogger(HealthCheckScheduler.class.getName());

    private final ScheduledExecutorService scheduler;
    private final RetryExecutor retryExecutor;
    private final List<HealthCheckResult> results = new CopyOnWriteArrayList<>();
    private final List<ScheduledFuture<?>> scheduledTasks = new ArrayList<>();

    public HealthCheckScheduler(RetryExecutor retryExecutor, int threadPoolSize) {
        this.retryExecutor = retryExecutor;
        this.scheduler = Executors.newScheduledThreadPool(threadPoolSize);
    }

    public void schedule(HealthCheck healthCheck, long intervalSeconds) {
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                HealthCheckResult result = retryExecutor.execute(healthCheck);
                results.add(result);
                log.info(() -> "Completed: " + result);
            } catch (Exception e) {
                log.warning(() -> "Scheduler error for " + healthCheck.getServiceName() + ": " + e.getMessage());
            }
        }, 0, intervalSeconds, TimeUnit.SECONDS);
        scheduledTasks.add(future);
    }

    public List<HealthCheckResult> getResults() {
        return Collections.unmodifiableList(results);
    }

    public void clearResults() {
        results.clear();
    }

    @Override
    public void close() {
        scheduledTasks.forEach(t -> t.cancel(false));
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
