package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckPriorityQueueTest {

    private HealthCheckPriorityQueue queue;

    @BeforeEach
    void setUp() {
        queue = new HealthCheckPriorityQueue();
    }

    @Test
    void testEnqueueAndDequeueInPriorityOrder() {
        queue.enqueue(new HealthCheckPriority("low", HealthCheckPriority.Level.LOW));
        queue.enqueue(new HealthCheckPriority("critical", HealthCheckPriority.Level.CRITICAL));
        queue.enqueue(new HealthCheckPriority("medium", HealthCheckPriority.Level.MEDIUM));

        HealthCheckPriority first = queue.dequeue();
        assertNotNull(first);
        assertEquals(HealthCheckPriority.Level.CRITICAL, first.getLevel());
    }

    @Test
    void testDequeueFromEmptyReturnsNull() {
        assertNull(queue.dequeue());
    }

    @Test
    void testPeekDoesNotRemove() {
        queue.enqueue(new HealthCheckPriority("svc", HealthCheckPriority.Level.HIGH));
        Optional<HealthCheckPriority> peeked = queue.peek();
        assertTrue(peeked.isPresent());
        assertEquals(1, queue.size());
    }

    @Test
    void testReenqueueUpdatesExisting() {
        queue.enqueue(new HealthCheckPriority("svc", HealthCheckPriority.Level.LOW));
        queue.enqueue(new HealthCheckPriority("svc", HealthCheckPriority.Level.CRITICAL));
        assertEquals(1, queue.size());
        assertEquals(HealthCheckPriority.Level.CRITICAL, queue.dequeue().getLevel());
    }

    @Test
    void testRemoveByName() {
        queue.enqueue(new HealthCheckPriority("svc", HealthCheckPriority.Level.HIGH));
        assertTrue(queue.remove("svc"));
        assertTrue(queue.isEmpty());
        assertFalse(queue.remove("nonexistent"));
    }

    @Test
    void testContains() {
        queue.enqueue(new HealthCheckPriority("db", HealthCheckPriority.Level.MEDIUM));
        assertTrue(queue.contains("db"));
        assertFalse(queue.contains("cache"));
    }

    @Test
    void testDrainAll() {
        queue.enqueue(new HealthCheckPriority("a", HealthCheckPriority.Level.HIGH));
        queue.enqueue(new HealthCheckPriority("b", HealthCheckPriority.Level.LOW));
        List<HealthCheckPriority> drained = queue.drainAll();
        assertEquals(2, drained.size());
        assertTrue(queue.isEmpty());
    }

    @Test
    void testSizeAndIsEmpty() {
        assertTrue(queue.isEmpty());
        queue.enqueue(new HealthCheckPriority("x", HealthCheckPriority.Level.MEDIUM));
        assertEquals(1, queue.size());
        assertFalse(queue.isEmpty());
    }
}
