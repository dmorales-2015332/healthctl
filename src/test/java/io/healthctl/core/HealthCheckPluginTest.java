package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckPluginTest {

    private HealthCheckPluginRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new HealthCheckPluginRegistry();
    }

    private HealthCheckPlugin createPlugin(String name, List<String> log) {
        return new HealthCheckPlugin() {
            @Override public String getName() { return name; }
            @Override public void beforeCheck(HealthCheck check) { log.add(name + ":before"); }
            @Override public void afterCheck(HealthCheck check, HealthCheckResult result) { log.add(name + ":after"); }
            @Override public void onError(HealthCheck check, Throwable t) { log.add(name + ":error"); }
        };
    }

    @Test
    void registerAndRetrievePlugin() {
        List<String> log = new ArrayList<>();
        HealthCheckPlugin plugin = createPlugin("audit", log);
        registry.register(plugin);
        assertTrue(registry.get("audit").isPresent());
        assertEquals(1, registry.size());
    }

    @Test
    void registerNullPluginThrows() {
        assertThrows(IllegalArgumentException.class, () -> registry.register(null));
    }

    @Test
    void unregisterPlugin() {
        List<String> log = new ArrayList<>();
        registry.register(createPlugin("metrics", log));
        assertTrue(registry.unregister("metrics"));
        assertFalse(registry.get("metrics").isPresent());
        assertFalse(registry.unregister("metrics"));
    }

    @Test
    void fireBeforeCheckInvokesAllPlugins() {
        List<String> log = new ArrayList<>();
        registry.register(createPlugin("p1", log));
        registry.register(createPlugin("p2", log));
        registry.fireBeforeCheck(null);
        assertTrue(log.contains("p1:before"));
        assertTrue(log.contains("p2:before"));
    }

    @Test
    void fireAfterCheckInvokesAllPlugins() {
        List<String> log = new ArrayList<>();
        registry.register(createPlugin("p1", log));
        registry.fireAfterCheck(null, null);
        assertTrue(log.contains("p1:after"));
    }

    @Test
    void fireOnErrorInvokesAllPlugins() {
        List<String> log = new ArrayList<>();
        registry.register(createPlugin("p1", log));
        registry.fireOnError(null, new RuntimeException("boom"));
        assertTrue(log.contains("p1:error"));
    }

    @Test
    void getAllReturnsPluginsInRegistrationOrder() {
        List<String> log = new ArrayList<>();
        registry.register(createPlugin("alpha", log));
        registry.register(createPlugin("beta", log));
        registry.register(createPlugin("gamma", log));
        List<HealthCheckPlugin> all = registry.getAll();
        assertEquals("alpha", all.get(0).getName());
        assertEquals("beta", all.get(1).getName());
        assertEquals("gamma", all.get(2).getName());
    }

    @Test
    void duplicateRegistrationReplacesExisting() {
        List<String> log1 = new ArrayList<>();
        List<String> log2 = new ArrayList<>();
        registry.register(createPlugin("dup", log1));
        registry.register(createPlugin("dup", log2));
        assertEquals(1, registry.size());
        registry.fireBeforeCheck(null);
        assertTrue(log2.contains("dup:before"));
        assertTrue(log1.isEmpty());
    }
}
