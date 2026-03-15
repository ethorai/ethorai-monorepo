package com.ai.therapists.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

final class TestEnvSupport {

    private static final Map<String, String> DOT_ENV = loadDotEnv();

    private TestEnvSupport() {
    }

    static String value(String key) {
        String system = System.getenv(key);
        if (system != null && !system.isBlank()) {
            return system;
        }

        String fromFile = DOT_ENV.get(key);
        return fromFile != null ? fromFile : "";
    }

    private static Map<String, String> loadDotEnv() {
        Map<String, String> values = new HashMap<>();
        Path envPath = Path.of(".env");

        if (!Files.exists(envPath)) {
            return values;
        }

        try {
            for (String line : Files.readAllLines(envPath)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }

                int idx = trimmed.indexOf('=');
                String key = trimmed.substring(0, idx).trim();
                String value = trimmed.substring(idx + 1).trim();
                values.put(key, value);
            }
        } catch (IOException ignored) {
            // If .env cannot be read, tests fall back to existing environment variables.
        }

        return values;
    }
}
