package io.healthctl.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckTrendAnalyzerTest {

    private HealthCheckTrendAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new HealthCheckTrendAnalyzer(6, 0.4, 0.7);
    }

    private HealthCheckResult success() {
        return new HealthCheckResult("svc", true, 0, "ok", Instant.now());
    }

    private HealthCheckResult failure() {
        return new HealthCheckResult("svc", false, 1, "err", Instant.now());
    }

    @Test
    void emptyResultsReturnStable() {
        assertEquals(HealthCheckTrendAnalyzer.Trend.STABLE, analyzer.evaluate(Collections.emptyList()));
    }

    @Test
    void nullResultsReturnStable() {
        assertEquals(HealthCheckTrendAnalyzer.Trend.STABLE, analyzer.evaluate(null));
    }

    @Test
    void allSuccessesReturnStable() {
        List<HealthCheckResult> results = List.of(success(), success(), success(), success());
        assertEquals(HealthCheckTrendAnalyzer.Trend.STABLE, analyzer.evaluate(results));
    }

    @Test
    void highFailureRateReturnsCritical() {
        List<HealthCheckResult> results = List.of(failure(), failure(), failure(), failure(), failure(), success());
        assertEquals(HealthCheckTrendAnalyzer.Trend.CRITICAL, analyzer.evaluate(results));
    }

    @Test
    void moderateFailureRateReturnsDegrading() {
        List<HealthCheckResult> results = List.of(success(), success(), success(), failure(), failure(), success());
        assertEquals(HealthCheckTrendAnalyzer.Trend.DEGRADING, analyzer.evaluate(results));
    }

    @Test
    void recentImprovementReturnsImproving() {
        // Earlier half: 2 failures; recent half: 0 failures
        List<HealthCheckResult> results = List.of(failure(), failure(), success(), success());
        assertEquals(HealthCheckTrendAnalyzer.Trend.IMPROVING, analyzer.evaluate(results));
    }

    @Test
    void windowSizeLimitsAnalysis() {
        // 10 results but window is 6; last 6 are all successes
        List<HealthCheckResult> results = List.of(
                failure(), failure(), failure(), failure(),
                success(), success(), success(), success(), success(), success());
        assertEquals(HealthCheckTrendAnalyzer.Trend.STABLE, analyzer.evaluate(results));
    }

    @Test
    void evaluateAllReturnsPerServiceTrends() {
        Map<String, List<HealthCheckResult>> grouped = Map.of(
                "db", List.of(success(), success()),
                "api", List.of(failure(), failure(), failure(), failure(), failure(), failure())
        );
        Map<String, HealthCheckTrendAnalyzer.Trend> trends = analyzer.evaluateAll(grouped);
        assertEquals(HealthCheckTrendAnalyzer.Trend.STABLE, trends.get("db"));
        assertEquals(HealthCheckTrendAnalyzer.Trend.CRITICAL, trends.get("api"));
    }

    @Test
    void constructorValidatesWindowSize() {
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckTrendAnalyzer(1, 0.4, 0.7));
    }

    @Test
    void constructorValidatesThresholdOrder() {
        assertThrows(IllegalArgumentException.class, () -> new HealthCheckTrendAnalyzer(5, 0.8, 0.5));
    }
}
