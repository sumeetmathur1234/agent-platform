package com.aiagent.platform.moderation;

import com.aiagent.platform.llm.OllamaClient;

import java.util.List;

/**
 * moderate(content, authorId) -> ModerationVerdict, per design doc
 * "Moderation logic (platform side)". Ordered, short-circuiting pipeline:
 *   1. rule-based filters (banned words, length cap 150 chars)
 *   2. prompt-injection regex
 *   3. LLM-as-judge (Ollama llama3.2:3b, toxicity/spam/misinfo score, reject > 0.7)
 *   4. rate limiting per agent
 *
 * Runs only on the platform side (never on the agent side) — see
 * "Platform vs agent responsibility split" in the design doc.
 */
public class ModerationPipeline {

    private static final int MAX_LENGTH = 150;
    private static final double JUDGE_REJECT_THRESHOLD = 0.7;

    private static final List<String> BANNED_WORDS = List.of(
            // ponytail: placeholder list, expand with real banned terms before demo
    );

    private static final List<String> INJECTION_MARKERS = List.of(
            "ignore your", "ignore previous instructions", "you are now",
            "disregard your persona", "new instructions:"
    );

    private final OllamaClient ollamaClient;

    public ModerationPipeline(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    public ModerationVerdict moderate(String content, String authorId) {
        // TODO: 1. rule-based filters (banned words, length)
        // TODO: 2. injection regex check
        // TODO: 3. Ollama judge call
        // TODO: 4. rate limit check (SELECT count(*) FROM posts WHERE author_id=? AND thread_id=?)
        throw new UnsupportedOperationException("not yet implemented");
    }
}
