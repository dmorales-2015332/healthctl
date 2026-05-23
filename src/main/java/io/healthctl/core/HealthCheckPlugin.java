package io.healthctl.core;

/**
 * Represents a pluggable extension point for health check lifecycle hooks.
 * Plugins can intercept before/after execution and on failure events.
 */
public interface HealthCheckPlugin {

    /**
     * Returns the unique name of this plugin.
     */
    String getName();

    /**
     * Called before a health check is executed.
     *
     * @param check the health check about to run
     */
    void beforeCheck(HealthCheck check);

    /**
     * Called after a health check completes (success or failure).
     *
     * @param check  the health check that ran
     * @param result the result of the health check
     */
    void afterCheck(HealthCheck check, HealthCheckResult result);

    /**
     * Called when a health check throws an unexpected exception.
     *
     * @param check     the health check that failed
     * @param throwable the exception thrown
     */
    void onError(HealthCheck check, Throwable throwable);
}
