package io.healthctl.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for managing {@link HealthCheckPlugin} instances.
 * Plugins are invoked in registration order.
 */
public class HealthCheckPluginRegistry {

    private final Map<String, HealthCheckPlugin> plugins = new LinkedHashMap<>();

    /**
     * Registers a plugin. Replaces any existing plugin with the same name.
     *
     * @param plugin the plugin to register
     */
    public void register(HealthCheckPlugin plugin) {
        if (plugin == null || plugin.getName() == null) {
            throw new IllegalArgumentException("Plugin and plugin name must not be null");
        }
        plugins.put(plugin.getName(), plugin);
    }

    /**
     * Unregisters a plugin by name.
     *
     * @param name the plugin name
     * @return true if a plugin was removed
     */
    public boolean unregister(String name) {
        return plugins.remove(name) != null;
    }

    /**
     * Returns a plugin by name.
     *
     * @param name the plugin name
     * @return an Optional containing the plugin if found
     */
    public Optional<HealthCheckPlugin> get(String name) {
        return Optional.ofNullable(plugins.get(name));
    }

    /**
     * Returns an unmodifiable list of all registered plugins in registration order.
     */
    public List<HealthCheckPlugin> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(plugins.values()));
    }

    /**
     * Returns the number of registered plugins.
     */
    public int size() {
        return plugins.size();
    }

    /**
     * Fires the beforeCheck event on all registered plugins.
     */
    public void fireBeforeCheck(HealthCheck check) {
        for (HealthCheckPlugin plugin : plugins.values()) {
            plugin.beforeCheck(check);
        }
    }

    /**
     * Fires the afterCheck event on all registered plugins.
     */
    public void fireAfterCheck(HealthCheck check, HealthCheckResult result) {
        for (HealthCheckPlugin plugin : plugins.values()) {
            plugin.afterCheck(check, result);
        }
    }

    /**
     * Fires the onError event on all registered plugins.
     */
    public void fireOnError(HealthCheck check, Throwable throwable) {
        for (HealthCheckPlugin plugin : plugins.values()) {
            plugin.onError(check, throwable);
        }
    }
}
