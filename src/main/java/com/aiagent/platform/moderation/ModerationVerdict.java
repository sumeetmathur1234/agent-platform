package com.aiagent.platform.moderation;

/**
 * Result of running ModerationPipeline.moderate(). See design doc
 * "Moderation logic (platform side)".
 */
public record ModerationVerdict(
        boolean approved,
        String reason,     // null if approved; too_long | banned_word | prompt_injection | toxic | rate_limited
        Double judgeScore  // null if rejected before reaching the LLM judge step
) {
    public static ModerationVerdict approved(Double judgeScore) {
        return new ModerationVerdict(true, null, judgeScore);
    }

    public static ModerationVerdict rejected(String reason) {
        return new ModerationVerdict(false, reason, null);
    }

    public static ModerationVerdict rejected(String reason, Double judgeScore) {
        return new ModerationVerdict(false, reason, judgeScore);
    }
}
