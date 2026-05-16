package io.healthctl.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Executes a shell-based health check command and captures its result.
 */
public class HealthCheck {

    private final String name;
    private final String command;
    private final int timeoutSeconds;

    public HealthCheck(String name, String command, int timeoutSeconds) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (command == null || command.isBlank()) throw new IllegalArgumentException("command must not be blank");
        if (timeoutSeconds <= 0) throw new IllegalArgumentException("timeoutSeconds must be positive");
        this.name = name;
        this.command = command;
        this.timeoutSeconds = timeoutSeconds;
    }

    public HealthCheckResult execute() {
        Instant start = Instant.now();
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return HealthCheckResult.failure(name, "Health check timed out after " + timeoutSeconds + "s",
                        start, Instant.now());
            }

            int exitCode = process.exitValue();
            String out = output.toString().trim();
            if (exitCode == 0) {
                return HealthCheckResult.success(name, out, start, Instant.now());
            } else {
                return HealthCheckResult.failure(name, "Exit code " + exitCode + ": " + out, start, Instant.now());
            }
        } catch (Exception e) {
            return HealthCheckResult.failure(name, "Exception: " + e.getMessage(), start, Instant.now());
        }
    }

    public String getName() { return name; }
    public String getCommand() { return command; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
}
