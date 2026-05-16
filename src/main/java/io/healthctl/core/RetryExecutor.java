package io.healthctl.core;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes a callable with retry semantics defined by a {@link RetryPolicy}.
 */
public class RetryExecutor {

    private static final Logger LOG = Logger.getLogger(RetryExecutor.class.getName());

    private final RetryPolicy policy;

    public RetryExecutor(RetryPolicy policy) {
        this.policy = policy;
    }

    /**
     * Executes the given task, retrying on failure according to the configured policy.
     *
     * @param task the callable to execute
     * @param <T>  return type
     * @return result of the callable
     * @throws RetryExhaustedException if all attempts fail
     */
    public <T> T execute(Callable<T> task) throws RetryExhaustedException {
        Exception lastException = null;

        for (int attempt = 1; attempt <= policy.getMaxAttempts(); attempt++) {
            try {
                LOG.fine(String.format("Attempt %d/%d", attempt, policy.getMaxAttempts()));
                return task.call();
            } catch (Exception e) {
                lastException = e;
                LOG.log(Level.WARNING, String.format("Attempt %d failed: %s", attempt, e.getMessage()));

                if (attempt < policy.getMaxAttempts()) {
                    long delay = policy.computeDelay(attempt);
                    LOG.fine(String.format("Waiting %d ms before next attempt", delay));
                    sleep(delay);
                }
            }
        }

        throw new RetryExhaustedException(
                String.format("All %d attempts failed", policy.getMaxAttempts()), lastException);
    }

    private void sleep(long ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
