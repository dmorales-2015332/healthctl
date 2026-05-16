package io.healthctl.core;

import java.util.*;

/**
 * Represents a directed dependency graph of health checks.
 * Allows ordering checks by dependency and detecting cycles.
 */
public class HealthCheckDependencyGraph {

    private final Map<String, Set<String>> adjacency = new LinkedHashMap<>();

    public void addCheck(String checkName) {
        adjacency.putIfAbsent(checkName, new LinkedHashSet<>());
    }

    public void addDependency(String checkName, String dependsOn) {
        adjacency.putIfAbsent(checkName, new LinkedHashSet<>());
        adjacency.putIfAbsent(dependsOn, new LinkedHashSet<>());
        adjacency.get(checkName).add(dependsOn);
    }

    public Set<String> getDependencies(String checkName) {
        return Collections.unmodifiableSet(adjacency.getOrDefault(checkName, Collections.emptySet()));
    }

    public Set<String> getChecks() {
        return Collections.unmodifiableSet(adjacency.keySet());
    }

    /**
     * Returns a topologically sorted list of check names (dependencies first).
     * Throws IllegalStateException if a cycle is detected.
     */
    public List<String> topologicalSort() {
        Map<String, Integer> state = new HashMap<>(); // 0=unvisited, 1=visiting, 2=visited
        LinkedList<String> sorted = new LinkedList<>();
        for (String node : adjacency.keySet()) {
            if (state.getOrDefault(node, 0) == 0) {
                visit(node, state, sorted);
            }
        }
        return Collections.unmodifiableList(sorted);
    }

    private void visit(String node, Map<String, Integer> state, LinkedList<String> sorted) {
        int s = state.getOrDefault(node, 0);
        if (s == 1) {
            throw new IllegalStateException("Cycle detected in health check dependency graph at: " + node);
        }
        if (s == 2) return;
        state.put(node, 1);
        for (String dep : adjacency.getOrDefault(node, Collections.emptySet())) {
            visit(dep, state, sorted);
        }
        state.put(node, 2);
        sorted.addFirst(node);
    }

    public boolean hasCycle() {
        try {
            topologicalSort();
            return false;
        } catch (IllegalStateException e) {
            return true;
        }
    }
}
