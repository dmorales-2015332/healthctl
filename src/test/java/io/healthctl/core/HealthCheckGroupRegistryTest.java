package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckGroupRegistryTest {

    private HealthCheckGroupRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new HealthCheckGroupRegistry();
    }

    @Test
    void register_shouldStoreGroup() {
        HealthCheckGroup group = new HealthCheckGroup("web", "Web checks");
        registry.register(group);
        assertTrue(registry.contains("web"));
        assertEquals(1, registry.size());
    }

    @Test
    void register_shouldThrowOnNull() {
        assertThrows(NullPointerException.class, () -> registry.register(null));
    }

    @Test
    void find_shouldReturnPresentOptionalForKnownGroup() {
        HealthCheckGroup group = new HealthCheckGroup("cache", "Cache checks");
        registry.register(group);
        Optional<HealthCheckGroup> found = registry.find("cache");
        assertTrue(found.isPresent());
        assertEquals("cache", found.get().getName());
    }

    @Test
    void find_shouldReturnEmptyOptionalForUnknownGroup() {
        assertTrue(registry.find("unknown").isEmpty());
    }

    @Test
    void unregister_shouldRemoveGroup() {
        registry.register(new HealthCheckGroup("mq", "Message queue checks"));
        assertTrue(registry.unregister("mq"));
        assertFalse(registry.contains("mq"));
    }

    @Test
    void unregister_shouldReturnFalseForMissingGroup() {
        assertFalse(registry.unregister("nonexistent"));
    }

    @Test
    void getAll_shouldReturnAllRegisteredGroups() {
        registry.register(new HealthCheckGroup("g1", "Group 1"));
        registry.register(new HealthCheckGroup("g2", "Group 2"));
        assertEquals(2, registry.getAll().size());
    }

    @Test
    void clear_shouldRemoveAllGroups() {
        registry.register(new HealthCheckGroup("g1", "Group 1"));
        registry.clear();
        assertEquals(0, registry.size());
    }

    @Test
    void toString_shouldContainRegisteredGroupNames() {
        registry.register(new HealthCheckGroup("svc", "Service checks"));
        assertTrue(registry.toString().contains("svc"));
    }
}
