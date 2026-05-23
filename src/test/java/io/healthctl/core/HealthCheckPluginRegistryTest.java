package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckPluginRegistryTest {

    private HealthCheckPluginRegistry registry;

    private static HealthCheckPlugin noopPlugin(String name) {
        return new HealthCheckPlugin() {
            @Override public String getName() { return name; }
            @Override public void beforeCheck(HealthCheck check) {}
            @Override public void afterCheck(HealthCheck check, HealthCheckResult result) {}
            @Override public void onError(HealthCheck check, Throwable t) {}
        };
    }

    @BeforeEach
    void setUp() {
        registry = new HealthCheckPluginRegistry();
    }

    @Test
    void emptyRegistryHasSizeZero() {
        assertEquals(0, registry.size());
    }

    @Test
    void getAllOnEmptyRegistryReturnsEmptyList() {
        assertTrue(registry.getAll().isEmpty());
    }

    @Test
    void getMissingPluginReturnsEmpty() {
        Optional<HealthCheckPlugin> result = registry.get("missing");
        assertFalse(result.isPresent());
    }

    @Test
    void registerIncreasesSize() {
        registry.register(noopPlugin("a"));
        registry.register(noopPlugin("b"));
        assertEquals(2, registry.size());
    }

    @Test
    void getAllIsUnmodifiable() {
        registry.register(noopPlugin("x"));
        List<HealthCheckPlugin> all = registry.getAll();
        assertThrows(UnsupportedOperationException.class, () -> all.add(noopPlugin("y")));
    }

    @Test
    void unregisterNonExistentReturnsFalse() {
        assertFalse(registry.unregister("ghost"));
    }

    @Test
    void pluginWithNullNameThrows() {
        HealthCheckPlugin badPlugin = new HealthCheckPlugin() {
            @Override public String getName() { return null; }
            @Override public void beforeCheck(HealthCheck check) {}
            @Override public void afterCheck(HealthCheck check, HealthCheckResult result) {}
            @Override public void onError(HealthCheck check, Throwable t) {}
        };
        assertThrows(IllegalArgumentException.class, () -> registry.register(badPlugin));
    }
}
