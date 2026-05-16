package io.healthctl.core;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckConfigLoaderTest {

    private final HealthCheckConfigLoader loader = new HealthCheckConfigLoader();

    private InputStream toStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void loadsBasicConfig() throws IOException {
        String props = "healthctl.checks.redis.command=redis-cli ping\n" +
                       "healthctl.checks.redis.timeout=5\n";
        List<HealthCheckConfig> configs = loader.load(toStream(props));
        assertEquals(1, configs.size());
        HealthCheckConfig cfg = configs.get(0);
        assertEquals("redis", cfg.getName());
        assertEquals("redis-cli ping", cfg.getCommand());
        assertEquals(5, cfg.getTimeoutSeconds());
    }

    @Test
    void loadsRetryPolicyFromConfig() throws IOException {
        String props = "healthctl.checks.db.command=pg_isready\n" +
                       "healthctl.checks.db.retry.maxAttempts=3\n" +
                       "healthctl.checks.db.retry.delayMs=200\n";
        List<HealthCheckConfig> configs = loader.load(toStream(props));
        assertEquals(1, configs.size());
        RetryPolicy policy = configs.get(0).getRetryPolicy();
        assertNotNull(policy);
        assertEquals(3, policy.getMaxAttempts());
        assertEquals(200L, policy.getDelayMs());
    }

    @Test
    void loadsTagsFromConfig() throws IOException {
        String props = "healthctl.checks.api.command=curl -f http://localhost/health\n" +
                       "healthctl.checks.api.tags=http,critical\n";
        List<HealthCheckConfig> configs = loader.load(toStream(props));
        List<String> tags = configs.get(0).getTags();
        assertEquals(2, tags.size());
        assertTrue(tags.contains("http"));
        assertTrue(tags.contains("critical"));
    }

    @Test
    void skipsCheckWithMissingCommand() throws IOException {
        String props = "healthctl.checks.ghost.timeout=10\n";
        List<HealthCheckConfig> configs = loader.load(toStream(props));
        assertTrue(configs.isEmpty());
    }

    @Test
    void loadsMultipleChecks() throws IOException {
        String props = "healthctl.checks.svc1.command=echo ok\n" +
                       "healthctl.checks.svc2.command=echo ok2\n";
        List<HealthCheckConfig> configs = loader.load(toStream(props));
        assertEquals(2, configs.size());
    }

    @Test
    void defaultTimeoutIsThirtySeconds() throws IOException {
        String props = "healthctl.checks.x.command=true\n";
        List<HealthCheckConfig> configs = loader.load(toStream(props));
        assertEquals(30, configs.get(0).getTimeoutSeconds());
    }
}
