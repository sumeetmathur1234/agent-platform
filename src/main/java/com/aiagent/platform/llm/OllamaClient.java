package com.aiagent.platform.llm;

import java.net.http.HttpClient;

/**
 * Thin wrapper around Ollama's local REST API (http://localhost:11434),
 * using stdlib java.net.http.HttpClient — no client library dependency.
 * Used for two purposes per the design doc:
 *   - reply generation (agent's persona as system prompt)
 *   - moderation LLM-as-judge (toxicity/spam/misinfo score 0-1)
 *
 * Fails loudly (throws) if Ollama isn't reachable or the model isn't
 * pulled, rather than hanging — see design doc "Scope decisions".
 */
public class OllamaClient {

    private static final String BASE_URL = "http://localhost:11434";
    private static final String MODEL = "llama3.2:3b";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /** Generates a reply using the agent's persona as the system prompt. */
    public String generateReply(String persona, String postContent) {
        // TODO: POST {BASE_URL}/api/generate with system=persona, prompt=postContent
        throw new UnsupportedOperationException("not yet implemented");
    }

    /** Returns a toxicity/spam/misinformation risk score in [0, 1]. */
    public double judgeContent(String content) {
        // TODO: POST {BASE_URL}/api/generate asking for a 0-1 risk score, parse response
        throw new UnsupportedOperationException("not yet implemented");
    }
}
