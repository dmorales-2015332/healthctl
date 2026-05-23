package io.healthctl.core;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable point-in-time snapshot of all health check results.
 */
public class HealthCheckSnapshot {

    private final String snapshotId;
    private final Instant capturedAt;
    private final List<HealthCheckResult> results;
    private final Map<String, String> metadata;

    public HealthCheckSnapshot(String snapshotId, Instant capturedAt,
                               List<HealthCheckResult> results,
                               Map<String, String> metadata) {
        this.snapshotId = Objects.requireNonNull(snapshotId, "snapshotId must not be null");
        this.capturedAt = Objects.requireNonNull(capturedAt, "capturedAt must not be null");
        this.results = Collections.unmodifiableList(
                Objects.requireNonNull(results, "results must not be null"));
        this.metadata = Collections.unmodifiableMap(
                Objects.requireNonNull(metadata, "metadata must not be null"));
    }

    public String getSnapshotId() { return snapshotId; }
    public Instant getCapturedAt() { return capturedAt; }
    public List<HealthCheckResult> getResults() { return results; }
    public Map<String, String> getMetadata() { return metadata; }

    public long countByStatus(String status) {
        return results.stream()
                .filter(r -> status.equalsIgnoreCase(r.getStatus()))
                .count();
    }

    public boolean isHealthy() {
        return results.stream().allMatch(r -> "UP".equalsIgnoreCase(r.getStatus()));
    }

    @Override
    public String toString() {
        return "HealthCheckSnapshot{id='" + snapshotId + "', capturedAt=" + capturedAt +
                ", total=" + results.size() + ", healthy=" + isHealthy() + "}";
    }
}
