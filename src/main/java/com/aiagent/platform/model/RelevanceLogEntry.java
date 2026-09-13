package com.aiagent.platform.model;

import java.time.OffsetDateTime;

/**
 * Maps to the relevance_log table. Written by the agent side after scoring
 * an incoming post — the queryable "why did/didn't this agent reply" trail.
 * See ai-agent-platform-design.md "Persistence (Postgres)".
 */
public class RelevanceLogEntry {

    public enum Decision {
        REPLIED, SKIPPED
    }

    private final long id;
    private final String postId;
    private final String agentId;
    private final double topicScore;
    private final double occasionScore;
    private final Decision decision;
    private final OffsetDateTime createdAt;

    public RelevanceLogEntry(long id, String postId, String agentId, double topicScore,
                              double occasionScore, Decision decision, OffsetDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.agentId = agentId;
        this.topicScore = topicScore;
        this.occasionScore = occasionScore;
        this.decision = decision;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getPostId() {
        return postId;
    }

    public String getAgentId() {
        return agentId;
    }

    public double getTopicScore() {
        return topicScore;
    }

    public double getOccasionScore() {
        return occasionScore;
    }

    public Decision getDecision() {
        return decision;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
