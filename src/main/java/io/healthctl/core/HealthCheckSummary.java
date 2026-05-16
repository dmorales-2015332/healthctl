package io.healthctl.core;

import java.time.Instant;
import java.util.List;

/**
 * Aggregates a filtered view of health check results into a summary,
 * providing counts and overall status for reporting or alerting.
 */
public class HealthCheckSummary {

    private final int totalChecks;
    private final int healthyCount;
    private final int unhealthyCount;
    private final int unknownCount;
    private final Instant generatedAt;
    private final List<HealthCheckResult> filteredResults;

    public HealthCheckSummary(List<HealthCheckResult> results) {
        this.filteredResults = results != null ? List.copyOf(results) : List.of();
        this.totalChecks = this.filteredResults.size();
        this.generatedAt = Instant.now();

        int healthy = 0, unhealthy = 0, unknown = 0;
        for (HealthCheckResult r : this.filteredResults) {
            if (r.getExitCode() < 0) {
                unknown++;
            } else if (r.isHealthy()) {
                healthy++;
            } else {
                unhealthy++;
            }
        }
        this.healthyCount = healthy;
        this.unhealthyCount = unhealthy;
        this.unknownCount = unknown;
    }

    public boolean isAllHealthy() {
        return unhealthyCount == 0 && unknownCount == 0 && totalChecks > 0;
    }

    public int getTotalChecks() { return totalChecks; }
    public int getHealthyCount() { return healthyCount; }
    public int getUnhealthyCount() { return unhealthyCount; }
    public int getUnknownCount() { return unknownCount; }
    public Instant getGeneratedAt() { return generatedAt; }
    public List<HealthCheckResult> getFilteredResults() { return filteredResults; }

    @Override
    public String toString() {
        return String.format(
                "HealthCheckSummary{total=%d, healthy=%d, unhealthy=%d, unknown=%d, allHealthy=%b}",
                totalChecks, healthyCount, unhealthyCount, unknownCount, isAllHealthy());
    }
}
