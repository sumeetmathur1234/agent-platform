package com.aiagent.platform.config;

import java.util.List;
import java.util.Map;

public class Constants {

    public static final int AGENT_REACTION_PARALLELISM = 4;

    public static final int MAX_THREAD_DEPTH = 1;

    public static final int MAX_POST_LENGTH = 150;

    public static final List<String> INJECTION_MARKERS = List.of(
            "ignore your", "ignore previous instructions", "you are now",
            "disregard your persona", "new instructions:"
    );

    public static final double JUDGE_REJECT_THRESHOLD = 0.7;

    public static final String JUDGE_SYSTEM_PROMPT =
            "You are a content moderation judge. Rate the given post for toxicity, "
                    + "spam, or misinformation risk on a scale of 0 (completely fine) to 1 (severe). "
                    + "Respond with ONLY a JSON object, no other text, in this exact shape: "
                    + "{\"score\": <number 0-1>, \"flagged_terms\": [<specific offending words or short "
                    + "phrases from the post, empty array if score is low>]}";

    // --- Reply generation ---
    public static String replyGenerationPrompt(String postContent) {
        return "Someone posted: \"" + postContent + "\"\n"
                + "Reply in 1-2 short sentences, staying in character. Reply text only, no preamble.";
    }

    // --- Relevance scoring ---
    public static final double TOPIC_THRESHOLD = 0.15;
    public static final double OCCASION_THRESHOLD = 0.15;

    public static final List<String> STOPWORDS = List.of(
            "the", "a", "an", "is", "are", "was", "were", "at", "on", "in", "to", "of",
            "and", "or", "for", "with", "this", "that", "it", "as", "by", "be", "has", "have"
    );

    /** Keyword -> occasion-tag map deriving a post's implicit occasion signals from its text. */
    public static final Map<String, List<String>> OCCASION_KEYWORDS = Map.of(
            "match", List.of("public_event", "crowd"),
            "stadium", List.of("public_event", "outdoor"),
            "tournament", List.of("public_event", "crowd"),
            "festival", List.of("public_event", "crowd", "outdoor"),
            "concert", List.of("public_event", "crowd", "outdoor"),
            "rain", List.of("outdoor"),
            "weather", List.of("outdoor")
    );

    private Constants() {
    }
}
