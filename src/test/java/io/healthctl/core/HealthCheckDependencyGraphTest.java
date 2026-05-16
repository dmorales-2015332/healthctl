package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckDependencyGraphTest {

    private HealthCheckDependencyGraph graph;

    @BeforeEach
    void setUp() {
        graph = new HealthCheckDependencyGraph();
    }

    @Test
    void testAddCheckRegistersNode() {
        graph.addCheck("db");
        assertTrue(graph.getChecks().contains("db"));
    }

    @Test
    void testAddDependencyRegistersEdge() {
        graph.addDependency("api", "db");
        assertTrue(graph.getDependencies("api").contains("db"));
    }

    @Test
    void testGetDependenciesReturnsEmptyForUnknown() {
        assertTrue(graph.getDependencies("unknown").isEmpty());
    }

    @Test
    void testTopologicalSortDependencyBeforeDependent() {
        graph.addDependency("api", "db");
        graph.addDependency("gateway", "api");
        List<String> sorted = graph.topologicalSort();
        assertTrue(sorted.indexOf("db") < sorted.indexOf("api"));
        assertTrue(sorted.indexOf("api") < sorted.indexOf("gateway"));
    }

    @Test
    void testTopologicalSortWithNoEdges() {
        graph.addCheck("a");
        graph.addCheck("b");
        List<String> sorted = graph.topologicalSort();
        assertEquals(2, sorted.size());
        assertTrue(sorted.contains("a") && sorted.contains("b"));
    }

    @Test
    void testCycleDetectionReturnsTrueOnCycle() {
        graph.addDependency("a", "b");
        graph.addDependency("b", "c");
        graph.addDependency("c", "a");
        assertTrue(graph.hasCycle());
    }

    @Test
    void testCycleDetectionReturnsFalseOnAcyclicGraph() {
        graph.addDependency("api", "db");
        graph.addDependency("cache", "db");
        assertFalse(graph.hasCycle());
    }

    @Test
    void testTopologicalSortThrowsOnCycle() {
        graph.addDependency("x", "y");
        graph.addDependency("y", "x");
        assertThrows(IllegalStateException.class, () -> graph.topologicalSort());
    }
}
