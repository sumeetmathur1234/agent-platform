package com.aiagent.platform.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized configuration — DB connection, Ollama endpoint/model.
 * Resolution order: OS environment variable, then a local .env file
 * (git-ignored, not committed), then a safe non-functional default.
 * No real credentials live in source — see .env (create it locally,
 * copy the shape from .env.example if present).
 */
public class AppConfig {

    private static final Map<String, String> DOT_ENV = loadDotEnv();

    // --- MySQL ---
    public static final String DB_URL = env("DB_URL", "jdbc:mysql://localhost:3306/ai_agent_platform");
    public static final String DB_USER = env("DB_USER", "root");
    public static final String DB_PASSWORD = env("DB_PASSWORD", "");

    // --- Ollama ---
    public static final String OLLAMA_BASE_URL = env("OLLAMA_BASE_URL", "http://localhost:11434");
    public static final String OLLAMA_MODEL = env("OLLAMA_MODEL", "llama3.2:3b");

    private static String env(String key, String defaultValue) {
        String osValue = System.getenv(key);
        if (osValue != null && !osValue.isBlank()) {
            return osValue;
        }
        String dotEnvValue = DOT_ENV.get(key);
        if (dotEnvValue != null && !dotEnvValue.isBlank()) {
            return dotEnvValue;
        }
        return defaultValue;
    }

    /** Reads KEY=VALUE lines from a .env file in the project root, if present. Never throws. */
    private static Map<String, String> loadDotEnv() {
        Map<String, String> values = new HashMap<>();
        Path path = Path.of(".env");
        if (!Files.exists(path)) {
            return values;
        }
        try {
            for (String line : Files.readAllLines(path)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }
                int idx = trimmed.indexOf('=');
                values.put(trimmed.substring(0, idx).trim(), trimmed.substring(idx + 1).trim());
            }
        } catch (IOException e) {
            // .env is optional/best-effort — fall through to OS env / defaults.
        }
        return values;
    }

    private AppConfig() {
    }
}
