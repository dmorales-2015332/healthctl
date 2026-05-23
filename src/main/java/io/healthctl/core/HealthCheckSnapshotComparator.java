package io.healthctl.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Compares two snapshots and produces a diff of status changes.
 */
public class HealthCheckSnapshotComparator {

    public record StatusChange(String checkName, String previousStatus, String currentStatus) {
        public boolean isDegradation() {
            return "UP".equalsIgnoreCase(previousStatus) && !"UP".equalsIgnoreCase(currentStatus);
        }
        public boolean isRecovery() {
            return !"UP".equalsIgnoreCase(previousStatus) && "UP".equalsIgnoreCase(currentStatus);
        }
    }

    public record SnapshotDiff(
            String baseSnapshotId,
            String targetSnapshotId,
            List<StatusChange> changes,
            List<String> addedChecks,
            List<String> removedChecks
    ) {
        public boolean hasChanges() {
            return !changes.isEmpty() || !addedChecks.isEmpty() || !removedChecks.isEmpty();
        }
    }

    public SnapshotDiff compare(HealthCheckSnapshot base, HealthCheckSnapshot target) {
        Map<String, String> baseMap = base.getResults().stream()
                .collect(Collectors.toMap(HealthCheckResult::getCheckName, HealthCheckResult::getStatus));
        Map<String, String> targetMap = target.getResults().stream()
                .collect(Collectors.toMap(HealthCheckResult::getCheckName, HealthCheckResult::getStatus));

        List<StatusChange> changes = new ArrayList<>();
        for (Map.Entry<String, String> entry : targetMap.entrySet()) {
            String name = entry.getKey();
            String targetStatus = entry.getValue();
            if (baseMap.containsKey(name) && !baseMap.get(name).equalsIgnoreCase(targetStatus)) {
                changes.add(new StatusChange(name, baseMap.get(name), targetStatus));
            }
        }

        List<String> added = targetMap.keySet().stream()
                .filter(k -> !baseMap.containsKey(k)).toList();
        List<String> removed = baseMap.keySet().stream()
                .filter(k -> !targetMap.containsKey(k)).toList();

        return new SnapshotDiff(base.getSnapshotId(), target.getSnapshotId(), changes, added, removed);
    }
}
