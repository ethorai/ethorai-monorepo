package com.ai.therapists.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads a local .env file into the Spring environment before context startup.
 * Only applies when the .env file exists (local dev). OS env vars take precedence.
 */
public class DotEnvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envFile = Path.of(".env");
        if (!Files.exists(envFile)) {
            return;
        }

        Map<String, Object> props = new LinkedHashMap<>();
        try {
            for (String line : Files.readAllLines(envFile)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }
                int idx = trimmed.indexOf('=');
                String key = trimmed.substring(0, idx).trim();
                String value = trimmed.substring(idx + 1).trim();
                // OS env vars win — only set if not already present
                if (environment.getProperty(key) == null) {
                    props.put(key, value);
                }
            }
        } catch (IOException ignored) {
            return;
        }

        if (!props.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource("dotenv", props));
        }
    }
}
