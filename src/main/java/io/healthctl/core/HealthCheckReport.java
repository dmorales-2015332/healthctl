package io.healthctl.core;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregates a list of HealthCheckResults into a summary report.
 */
public class HealthCheckReport {

    private final List<HealthCheckResult> results;

    public HealthCheckReport(List<HealthCheckResult> results) {
        this.results = List.copyOf(results);
    }

    public long totalChecks() {
        return results.size();
    }

    public long healthyCount() {
        return results.stream().filter(HealthCheckResult::isHealthy).count();
    }

    public long unhealthyCount() {
        return results.stream()
                .filter(r -> r.getStatus() == HealthCheckResult.Status.UNHEALTHY)
                .count();
    }

    public Map<String, Long> countByService() {
        return results.stream()
                .collect(Collectors.groupingBy(HealthCheckResult::getServiceName, Collectors.counting()));
    }

    public Map<String, Long> unhealthyByService() {
        return results.stream()
                .filter(r -> !r.isHealthy())
                .collect(Collectors.groupingBy(HealthCheckResult::getServiceName, Collectors.counting()));
    }

    public double averageDurationMillis() {
        return results.stream()
                .mapToLong(HealthCheckResult::getDurationMillis)
                .average()
                .orElse(0.0);
    }

    @Override
    public String toString() {
        return String.format(
                "HealthCheckReport{total=%d, healthy=%d, unhealthy=%d, avgDuration=%.1fms}",
                totalChecks(), healthyCount(), unhealthyCount(), averageDurationMillis());
    }
}
