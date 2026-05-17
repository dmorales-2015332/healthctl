package io.healthctl.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckTagTest {

    @Test
    void shouldCreateTagWithName() {
        HealthCheckTag tag = HealthCheckTag.builder("database").build();
        assertEquals("database", tag.getName());
        assertTrue(tag.getDescription().isEmpty());
        assertTrue(tag.getAttributes().isEmpty());
    }

    @Test
    void shouldCreateTagWithAllFields() {
        HealthCheckTag tag = HealthCheckTag.builder("network")
                .description("Network-related checks")
                .attribute("critical")
                .attribute("external")
                .build();

        assertEquals("network", tag.getName());
        assertEquals("Network-related checks", tag.getDescription());
        assertTrue(tag.hasAttribute("critical"));
        assertTrue(tag.hasAttribute("external"));
        assertFalse(tag.hasAttribute("internal"));
    }

    @Test
    void shouldBeEqualIgnoringCase() {
        HealthCheckTag tag1 = HealthCheckTag.builder("Database").build();
        HealthCheckTag tag2 = HealthCheckTag.builder("database").build();
        assertEquals(tag1, tag2);
        assertEquals(tag1.hashCode(), tag2.hashCode());
    }

    @Test
    void shouldThrowWhenNameIsNull() {
        assertThrows(NullPointerException.class, () -> HealthCheckTag.builder(null).build());
    }

    @Test
    void shouldReturnUnmodifiableAttributes() {
        HealthCheckTag tag = HealthCheckTag.builder("cache").attribute("fast").build();
        assertThrows(UnsupportedOperationException.class, () -> tag.getAttributes().add("new"));
    }

    @Test
    void shouldHaveReadableToString() {
        HealthCheckTag tag = HealthCheckTag.builder("storage").description("Storage checks").build();
        String str = tag.toString();
        assertTrue(str.contains("storage"));
        assertTrue(str.contains("Storage checks"));
    }
}
