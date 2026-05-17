package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckTagRegistryTest {

    private HealthCheckTagRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new HealthCheckTagRegistry();
    }

    @Test
    void shouldRegisterCheckWithTags() {
        registry.register("db-check", List.of("database", "critical"));
        assertTrue(registry.getChecksByTag("database").contains("db-check"));
        assertTrue(registry.getChecksByTag("critical").contains("db-check"));
    }

    @Test
    void shouldReturnTagsForCheck() {
        registry.register("cache-check", List.of("cache", "fast"));
        Set<String> tags = registry.getTagsForCheck("cache-check");
        assertTrue(tags.contains("cache"));
        assertTrue(tags.contains("fast"));
    }

    @Test
    void shouldDeregisterCheck() {
        registry.register("net-check", List.of("network"));
        registry.deregister("net-check");
        assertFalse(registry.getChecksByTag("network").contains("net-check"));
        assertTrue(registry.getTagsForCheck("net-check").isEmpty());
    }

    @Test
    void shouldReturnAllTags() {
        registry.register("check1", List.of("alpha", "beta"));
        registry.register("check2", List.of("gamma"));
        Set<String> allTags = registry.getAllTags();
        assertTrue(allTags.containsAll(List.of("alpha", "beta", "gamma")));
    }

    @Test
    void shouldGetChecksByAnyTag() {
        registry.register("check-a", List.of("web"));
        registry.register("check-b", List.of("db"));
        Set<String> results = registry.getChecksByAnyTag(List.of("web", "db"));
        assertTrue(results.contains("check-a"));
        assertTrue(results.contains("check-b"));
    }

    @Test
    void shouldGetChecksByAllTags() {
        registry.register("check-x", List.of("critical", "external"));
        registry.register("check-y", List.of("critical"));
        Set<String> results = registry.getChecksByAllTags(List.of("critical", "external"));
        assertTrue(results.contains("check-x"));
        assertFalse(results.contains("check-y"));
    }

    @Test
    void shouldThrowOnBlankCheckName() {
        assertThrows(IllegalArgumentException.class, () -> registry.register("", List.of("tag")));
    }

    @Test
    void shouldNormalizeTagsToLowercase() {
        registry.register("svc-check", List.of("CRITICAL", "Network"));
        assertTrue(registry.hasTag("svc-check", "critical"));
        assertTrue(registry.hasTag("svc-check", "network"));
    }

    @Test
    void shouldClearAllRegistrations() {
        registry.register("check1", List.of("tag1"));
        registry.clear();
        assertTrue(registry.getAllTags().isEmpty());
        assertTrue(registry.getTagsForCheck("check1").isEmpty());
    }
}
