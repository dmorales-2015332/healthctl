package io.healthctl.core;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * A thread-safe priority queue for scheduling health checks based on their
 * assigned priority levels. Higher-priority (CRITICAL) checks are dequeued first.
 */
public class HealthCheckPriorityQueue {

    private final PriorityBlockingQueue<HealthCheckPriority> queue;
    private final Map<String, HealthCheckPriority> priorityIndex;

    public HealthCheckPriorityQueue() {
        this.queue = new PriorityBlockingQueue<>();
        this.priorityIndex = new HashMap<>();
    }

    public synchronized void enqueue(HealthCheckPriority priority) {
        Objects.requireNonNull(priority, "priority must not be null");
        String name = priority.getCheckName();
        if (priorityIndex.containsKey(name)) {
            queue.remove(priorityIndex.get(name));
        }
        priorityIndex.put(name, priority);
        queue.offer(priority);
    }

    public synchronized HealthCheckPriority dequeue() {
        HealthCheckPriority p = queue.poll();
        if (p != null) {
            priorityIndex.remove(p.getCheckName());
        }
        return p;
    }

    public synchronized Optional<HealthCheckPriority> peek() {
        return Optional.ofNullable(queue.peek());
    }

    public synchronized boolean remove(String checkName) {
        Objects.requireNonNull(checkName, "checkName must not be null");
        HealthCheckPriority p = priorityIndex.remove(checkName);
        if (p != null) {
            return queue.remove(p);
        }
        return false;
    }

    public synchronized boolean contains(String checkName) {
        return priorityIndex.containsKey(checkName);
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized List<HealthCheckPriority> drainAll() {
        List<HealthCheckPriority> result = new ArrayList<>();
        queue.drainTo(result);
        priorityIndex.clear();
        return result;
    }
}
