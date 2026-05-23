package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckStatusAggregatorTest {

    private HealthCheckStatusAggregator aggregator;

    @BeforeEach
    void setUp() {
        aggregator = new HealthCheckStatusAggregator();
    }

    private HealthCheckResult success(String name) {
        return new HealthCheckResult(name, true, 0, "OK", Instant.now(), 10L);
    }

    private HealthCheckResult failure(String name) {
        return new HealthCheckResult(name, false, 1, "FAIL", Instant.now(), 10L);
    }

    @Test
    void emptyCollectionReturnsUnknown() {
        assertEquals(HealthCheckStatusAggregator.Status.UNKNOWN,
                aggregator.aggregate(List.of()));
    }

    @Test
    void allSuccessReturnsUp() {
        assertEquals(HealthCheckStatusAggregator.Status.UP,
                aggregator.aggregate(List.of(success("svc-a"), success("svc-b"))));
    }

    @Test
    void anyFailureReturnsDown() {
        assertEquals(HealthCheckStatusAggregator.Status.DOWN,
                aggregator.aggregate(List.of(success("svc-a"), failure("svc-b"))));
    }

    @Test
    void allFailuresReturnDown() {
        assertEquals(HealthCheckStatusAggregator.Status.DOWN,
                aggregator.aggregate(List.of(failure("svc-a"), failure("svc-b"))));
    }

    @Test
    void nullCollectionThrows() {
        assertThrows(NullPointerException.class, () -> aggregator.aggregate(null));
    }

    @Test
    void statusCountsAreCorrect() {
        List<HealthCheckResult> results = List.of(
                success("svc-a"), success("svc-b"), failure("svc-c"));

        Map<HealthCheckStatusAggregator.Status, Long> counts = aggregator.statusCounts(results);

        assertEquals(2L, counts.get(HealthCheckStatusAggregator.Status.UP));
        assertEquals(1L, counts.get(HealthCheckStatusAggregator.Status.DOWN));
        assertEquals(0L, counts.get(HealthCheckStatusAggregator.Status.UNKNOWN));
    }

    @Test
    void statusCountsEmptyCollection() {
        Map<HealthCheckStatusAggregator.Status, Long> counts = aggregator.statusCounts(List.of());
        for (long v : counts.values()) {
            assertEquals(0L, v);
        }
    }

    @Test
    void nullStatusCountsThrows() {
        assertThrows(NullPointerException.class, () -> aggregator.statusCounts(null));
    }
}
