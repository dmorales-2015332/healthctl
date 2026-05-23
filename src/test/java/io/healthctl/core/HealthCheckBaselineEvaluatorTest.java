package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckBaselineEvaluatorTest {

    private HealthCheckBaselineStore store;
    private HealthCheckBaselineEvaluator evaluator;

    @BeforeEach
    void setUp() {
        store = new HealthCheckBaselineStore();
        evaluator = new HealthCheckBaselineEvaluator(store);
    }

    private HealthCheckResult okResult(String checkId) {
        return new HealthCheckResult(checkId, true, "OK", 0);
    }

    @Test
    void evaluate_noBaseline_returnsEmptyDeviations() {
        List<HealthCheckBaselineEvaluator.BaselineDeviation> result =
                evaluator.evaluate(okResult("svc-api"), 100L, 0.99);
        assertTrue(result.isEmpty());
    }

    @Test
    void evaluate_withinBaseline_returnsEmptyDeviations() {
        store.save(new HealthCheckBaseline("svc-api", 0.95, 200L, null));
        List<HealthCheckBaselineEvaluator.BaselineDeviation> result =
                evaluator.evaluate(okResult("svc-api"), 150L, 0.97);
        assertTrue(result.isEmpty());
    }

    @Test
    void evaluate_latencyExceeded_returnsDeviation() {
        store.save(new HealthCheckBaseline("svc-api", 0.95, 200L, null));
        List<HealthCheckBaselineEvaluator.BaselineDeviation> deviations =
                evaluator.evaluate(okResult("svc-api"), 350L, 0.97);
        assertEquals(1, deviations.size());
        assertEquals(HealthCheckBaselineEvaluator.DeviationType.LATENCY_EXCEEDED,
                deviations.get(0).type());
        assertEquals(200.0, deviations.get(0).baselineValue());
        assertEquals(350.0, deviations.get(0).actualValue());
    }

    @Test
    void evaluate_successRateDegraded_returnsDeviation() {
        store.save(new HealthCheckBaseline("svc-db", 0.95, 300L, null));
        List<HealthCheckBaselineEvaluator.BaselineDeviation> deviations =
                evaluator.evaluate(okResult("svc-db"), 100L, 0.80);
        assertEquals(1, deviations.size());
        assertEquals(HealthCheckBaselineEvaluator.DeviationType.SUCCESS_RATE_DEGRADED,
                deviations.get(0).type());
    }

    @Test
    void evaluate_bothExceeded_returnsTwoDeviations() {
        store.save(new HealthCheckBaseline("svc-cache", 0.90, 100L, null));
        List<HealthCheckBaselineEvaluator.BaselineDeviation> deviations =
                evaluator.evaluate(okResult("svc-cache"), 500L, 0.70);
        assertEquals(2, deviations.size());
    }

    @Test
    void evaluate_nullResult_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> evaluator.evaluate(null, 100L, 0.99));
    }

    @Test
    void hasBaseline_reflectsStoreState() {
        assertFalse(evaluator.hasBaseline("svc-x"));
        store.save(new HealthCheckBaseline("svc-x", 0.9, 200L, null));
        assertTrue(evaluator.hasBaseline("svc-x"));
    }

    @Test
    void constructor_nullStore_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new HealthCheckBaselineEvaluator(null));
    }
}
