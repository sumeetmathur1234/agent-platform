package com.aiagent.platform.moderation;

public record ModerationVerdict(
        boolean approved,
        String reason,
        Double judgeScore
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
