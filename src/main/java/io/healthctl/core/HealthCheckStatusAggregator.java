package io.healthctl.core;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Aggregates multiple {@link HealthCheckResult} instances into a single
 * overall status, following a severity-based precedence rule:
 * DOWN > DEGRADED > UP > UNKNOWN.
 */
public class HealthCheckStatusAggregator {

    public enum Status {
        UNKNOWN, UP, DEGRADED, DOWN
    }

    /**
     * Compute the aggregate status from a collection of results.
     *
     * @param results the individual health-check results; must not be null
     * @return the worst-case {@link Status} across all results
     */
    public Status aggregate(Collection<HealthCheckResult> results) {
        Objects.requireNonNull(results, "results must not be null");

        if (results.isEmpty()) {
            return Status.UNKNOWN;
        }

        Status worst = Status.UNKNOWN;
        for (HealthCheckResult result : results) {
            Status current = toStatus(result);
            if (current.ordinal() > worst.ordinal()) {
                worst = current;
            }
            if (worst == Status.DOWN) {
                break; // cannot get worse
            }
        }
        return worst;
    }

    /**
     * Returns a frequency map of statuses across all results.
     *
     * @param results the individual health-check results; must not be null
     * @return map of {@link Status} to occurrence count
     */
    public Map<Status, Long> statusCounts(Collection<HealthCheckResult> results) {
        Objects.requireNonNull(results, "results must not be null");

        Map<Status, Long> counts = new EnumMap<>(Status.class);
        for (Status s : Status.values()) {
            counts.put(s, 0L);
        }
        for (HealthCheckResult result : results) {
            Status s = toStatus(result);
            counts.merge(s, 1L, Long::sum);
        }
        return counts;
    }

    private Status toStatus(HealthCheckResult result) {
        if (result == null) {
            return Status.UNKNOWN;
        }
        if (!result.isSuccess()) {
            // treat a non-zero exit code as DOWN; partial/degraded not yet modelled
            return Status.DOWN;
        }
        return Status.UP;
    }
}
