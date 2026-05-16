package io.healthctl.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Loads {@link HealthCheckConfig} instances from a properties file.
 *
 * <p>Expected format per check (prefix = healthctl.checks.&lt;name&gt;):
 * <pre>
 *   healthctl.checks.mydb.command=pg_isready -h localhost
 *   healthctl.checks.mydb.timeout=10
 *   healthctl.checks.mydb.retry.maxAttempts=3
 *   healthctl.checks.mydb.retry.delayMs=500
 *   healthctl.checks.mydb.tags=db,critical
 * </pre>
 */
public class HealthCheckConfigLoader {

    private static final Logger LOG = Logger.getLogger(HealthCheckConfigLoader.class.getName());
    private static final String PREFIX = "healthctl.checks.";

    public List<HealthCheckConfig> load(InputStream inputStream) throws IOException {
        Properties props = new Properties();
        props.load(inputStream);
        return parseConfigs(props);
    }

    private List<HealthCheckConfig> parseConfigs(Properties props) {
        List<String> names = resolveCheckNames(props);
        List<HealthCheckConfig> configs = new ArrayList<>();

        for (String name : names) {
            String base = PREFIX + name + ".";
            String command = props.getProperty(base + "command");
            if (command == null || command.isBlank()) {
                LOG.warning("Skipping check '" + name + "': missing command");
                continue;
            }

            int timeout = parseInt(props.getProperty(base + "timeout"), 30);
            int maxAttempts = parseInt(props.getProperty(base + "retry.maxAttempts"), 1);
            long delayMs = parseLong(props.getProperty(base + "retry.delayMs"), 0L);
            List<String> tags = parseTags(props.getProperty(base + "tags"));

            RetryPolicy retryPolicy = RetryPolicy.of(maxAttempts, delayMs);
            HealthCheckConfig config = HealthCheckConfig.builder(name, command)
                    .retryPolicy(retryPolicy)
                    .timeoutSeconds(timeout)
                    .tags(tags)
                    .build();
            configs.add(config);
            LOG.fine("Loaded config for check: " + name);
        }
        return configs;
    }

    private List<String> resolveCheckNames(Properties props) {
        List<String> names = new ArrayList<>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(PREFIX)) {
                String remainder = key.substring(PREFIX.length());
                String checkName = remainder.contains(".") ? remainder.substring(0, remainder.indexOf('.')) : remainder;
                if (!names.contains(checkName)) names.add(checkName);
            }
        }
        return names;
    }

    private List<String> parseTags(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private int parseInt(String value, int defaultValue) {
        try { return value != null ? Integer.parseInt(value.trim()) : defaultValue; }
        catch (NumberFormatException e) { return defaultValue; }
    }

    private long parseLong(String value, long defaultValue) {
        try { return value != null ? Long.parseLong(value.trim()) : defaultValue; }
        catch (NumberFormatException e) { return defaultValue; }
    }
}
