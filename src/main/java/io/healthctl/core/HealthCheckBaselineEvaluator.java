package io.healthctl.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Evaluates health check results against stored baselines and
 * produces deviation reports when thresholds are breached.
 */
public class HealthCheckBaselineEvaluator {

    public enum DeviationType { LATENCY_EXCEEDED, SUCCESS_RATE_DEGRADED }

    public record BaselineDeviation(String checkId, DeviationType type,
                                    double baselineValue, double actualValue) {
        @Override
        public String toString() {
            return "BaselineDeviation{checkId='" + checkId + "', type=" + type +
                   ", baseline=" + baselineValue + ", actual=" + actualValue + "}";
        }
    }

    private final HealthCheckBaselineStore store;

    public HealthCheckBaselineEvaluator(HealthCheckBaselineStore store) {
        if (store == null) throw new IllegalArgumentException("store must not be null");
        this.store = store;
    }

    /**
     * Evaluates a single result against its baseline.
     *
     * @param result       the health check result to evaluate
     * @param latencyMs    the observed latency in milliseconds
     * @param successRate  the rolling success rate (0.0–1.0)
     * @return list of deviations; empty if within baseline
     */
    public List<BaselineDeviation> evaluate(HealthCheckResult result,
                                             long latencyMs, double successRate) {
        if (result == null) throw new IllegalArgumentException("result must not be null");

        List<BaselineDeviation> deviations = new ArrayList<>();
        Optional<HealthCheckBaseline> opt = store.findById(result.getCheckId());
        if (opt.isEmpty()) return deviations;

        HealthCheckBaseline baseline = opt.get();

        if (!baseline.isLatencyWithinBaseline(latencyMs)) {
            deviations.add(new BaselineDeviation(
                    result.getCheckId(),
                    DeviationType.LATENCY_EXCEEDED,
                    baseline.getExpectedMaxLatencyMs(),
                    latencyMs));
        }

        if (!baseline.isSuccessRateWithinBaseline(successRate)) {
            deviations.add(new BaselineDeviation(
                    result.getCheckId(),
                    DeviationType.SUCCESS_RATE_DEGRADED,
                    baseline.getExpectedSuccessRate(),
                    successRate));
        }

        return deviations;
    }

    public boolean hasBaseline(String checkId) {
        return store.contains(checkId);
    }
}
