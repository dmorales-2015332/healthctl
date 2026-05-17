package io.healthctl.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for managing tags associated with health checks.
 * Supports grouping and filtering checks by tag.
 */
public class HealthCheckTagRegistry {

    private final Map<String, Set<String>> tagToChecks = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> checkToTags = new ConcurrentHashMap<>();

    public void register(String checkName, Collection<String> tags) {
        if (checkName == null || checkName.isBlank()) {
            throw new IllegalArgumentException("Check name must not be null or blank");
        }
        Set<String> normalizedTags = tags == null ? Collections.emptySet() :
                tags.stream().map(String::toLowerCase).collect(Collectors.toSet());

        checkToTags.put(checkName, new HashSet<>(normalizedTags));
        for (String tag : normalizedTags) {
            tagToChecks.computeIfAbsent(tag, k -> ConcurrentHashMap.newKeySet()).add(checkName);
        }
    }

    public void deregister(String checkName) {
        Set<String> tags = checkToTags.remove(checkName);
        if (tags != null) {
            for (String tag : tags) {
                Set<String> checks = tagToChecks.get(tag);
                if (checks != null) {
                    checks.remove(checkName);
                    if (checks.isEmpty()) {
                        tagToChecks.remove(tag);
                    }
                }
            }
        }
    }

    public Set<String> getChecksByTag(String tag) {
        return Collections.unmodifiableSet(
                tagToChecks.getOrDefault(tag.toLowerCase(), Collections.emptySet()));
    }

    public Set<String> getTagsForCheck(String checkName) {
        return Collections.unmodifiableSet(
                checkToTags.getOrDefault(checkName, Collections.emptySet()));
    }

    public Set<String> getAllTags() {
        return Collections.unmodifiableSet(tagToChecks.keySet());
    }

    public Set<String> getChecksByAnyTag(Collection<String> tags) {
        return tags.stream()
                .map(String::toLowerCase)
                .flatMap(tag -> getChecksByTag(tag).stream())
                .collect(Collectors.toSet());
    }

    public Set<String> getChecksByAllTags(Collection<String> tags) {
        List<String> normalized = tags.stream().map(String::toLowerCase).collect(Collectors.toList());
        return checkToTags.entrySet().stream()
                .filter(e -> e.getValue().containsAll(normalized))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public boolean hasTag(String checkName, String tag) {
        Set<String> tags = checkToTags.get(checkName);
        return tags != null && tags.contains(tag.toLowerCase());
    }

    public void clear() {
        tagToChecks.clear();
        checkToTags.clear();
    }
}
