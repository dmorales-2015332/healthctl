package io.healthctl.core;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Filters health check results based on configurable criteria such as
 * status, service name pattern, or result severity.
 */
public class HealthCheckFilter {

    public enum StatusFilter {
        ALL, HEALTHY, UNHEALTHY, UNKNOWN
    }

    private final StatusFilter statusFilter;
    private final String serviceNamePattern;

    public HealthCheckFilter(StatusFilter statusFilter, String serviceNamePattern) {
        this.statusFilter = statusFilter != null ? statusFilter : StatusFilter.ALL;
        this.serviceNamePattern = serviceNamePattern;
    }

    public HealthCheckFilter(StatusFilter statusFilter) {
        this(statusFilter, null);
    }

    public static HealthCheckFilter all() {
        return new HealthCheckFilter(StatusFilter.ALL, null);
    }

    public static HealthCheckFilter unhealthyOnly() {
        return new HealthCheckFilter(StatusFilter.UNHEALTHY, null);
    }

    public List<HealthCheckResult> apply(List<HealthCheckResult> results) {
        if (results == null) {
            return List.of();
        }
        return results.stream()
                .filter(this::matchesStatus)
                .filter(this::matchesServiceName)
                .collect(Collectors.toList());
    }

    private boolean matchesStatus(HealthCheckResult result) {
        return switch (statusFilter) {
            case ALL -> true;
            case HEALTHY -> result.isHealthy();
            case UNHEALTHY -> !result.isHealthy();
            case UNKNOWN -> result.getExitCode() < 0;
        };
    }

    private boolean matchesServiceName(HealthCheckResult result) {
        if (serviceNamePattern == null || serviceNamePattern.isBlank()) {
            return true;
        }
        String name = result.getServiceName();
        return name != null && name.matches(serviceNamePattern);
    }

    public StatusFilter getStatusFilter() {
        return statusFilter;
    }

    public String getServiceNamePattern() {
        return serviceNamePattern;
    }
}
