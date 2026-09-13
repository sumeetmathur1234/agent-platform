package com.aiagent.platform.model;

import java.time.OffsetDateTime;

/**
 * Maps to the moderation_log table. Written by the platform side after
 * running ModerationPipeline.moderate() on a post (root or reply).
 * See ai-agent-platform-design.md "Persistence (Postgres)".
 */
public class ModerationLogEntry {

    private final long id;
    private final String postId;
    private final Post.ModerationStatus verdict; // APPROVED or REJECTED (never PENDING here)
    private final String reason;                  // null if approved
    private final Double judgeScore;                // null if rejected before reaching the LLM judge
    private final OffsetDateTime createdAt;

    public ModerationLogEntry(long id, String postId, Post.ModerationStatus verdict,
                               String reason, Double judgeScore, OffsetDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.verdict = verdict;
        this.reason = reason;
        this.judgeScore = judgeScore;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getPostId() {
        return postId;
    }

    public Post.ModerationStatus getVerdict() {
        return verdict;
    }

    public String getReason() {
        return reason;
    }

    public Double getJudgeScore() {
        return judgeScore;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
