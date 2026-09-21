package com.aiagent.platform.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AppConfig {

    private static final Map<String, String> DOT_ENV = loadDotEnv();

    public static final String DB_URL = env("DB_URL", "jdbc:mysql://localhost:3306/ai_agent_platform");
    public static final String DB_USER = env("DB_USER", "");
    public static final String DB_PASSWORD = env("DB_PASSWORD", "");

    public static final String OLLAMA_BASE_URL = env("OLLAMA_BASE_URL", "http://localhost:11434");
    public static final String OLLAMA_MODEL = env("OLLAMA_MODEL", "llama3.2:3b");
    public static final boolean CHAT_UI_MODE = Boolean.parseBoolean(env("CHAT_UI_MODE", "false"));

    public static final String RELEVANCE_SCORER_VERSION = env("RELEVANCE_SCORER_VERSION", "v1");

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
