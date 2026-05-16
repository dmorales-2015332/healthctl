package io.healthctl.core;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryExecutorTest {

    @Test
    void succeedsOnFirstAttempt() throws RetryExhaustedException {
        RetryPolicy policy = RetryPolicy.builder().maxAttempts(3).build();
        RetryExecutor executor = new RetryExecutor(policy);

        String result = executor.execute(() -> "ok");
        assertEquals("ok", result);
    }

    @Test
    void retriesAndSucceedsOnThirdAttempt() throws RetryExhaustedException {
        AtomicInteger counter = new AtomicInteger(0);
        RetryPolicy policy = RetryPolicy.builder()
                .maxAttempts(3)
                .initialDelayMs(0)
                .build();
        RetryExecutor executor = new RetryExecutor(policy);

        String result = executor.execute(() -> {
            if (counter.incrementAndGet() < 3) {
                throw new RuntimeException("not ready");
            }
            return "healthy";
        });

        assertEquals("healthy", result);
        assertEquals(3, counter.get());
    }

    @Test
    void throwsRetryExhaustedAfterAllAttemptsFail() {
        RetryPolicy policy = RetryPolicy.builder()
                .maxAttempts(2)
                .initialDelayMs(0)
                .build();
        RetryExecutor executor = new RetryExecutor(policy);

        RetryExhaustedException ex = assertThrows(RetryExhaustedException.class, () ->
                executor.execute(() -> { throw new RuntimeException("always fails"); })
        );

        assertTrue(ex.getMessage().contains("2"));
        assertNotNull(ex.getCause());
    }

    @Test
    void exponentialBackoffDelayGrowsCorrectly() {
        RetryPolicy policy = RetryPolicy.builder()
                .strategy(RetryPolicy.Strategy.EXPONENTIAL_BACKOFF)
                .initialDelayMs(100)
                .maxDelayMs(1000)
                .build();

        assertEquals(100,  policy.computeDelay(1));
        assertEquals(200,  policy.computeDelay(2));
        assertEquals(400,  policy.computeDelay(3));
        assertEquals(1000, policy.computeDelay(10)); // capped at maxDelayMs
    }
}
