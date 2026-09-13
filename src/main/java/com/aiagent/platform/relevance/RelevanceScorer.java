package com.aiagent.platform.relevance;

import com.aiagent.platform.model.Agent;
import com.aiagent.platform.model.Post;

/**
 * Cosine similarity relevance scoring, fully deterministic, no LLM.
 * See design doc "Relevance logic (agent side)". Runs on the agent's own
 * side (never on the platform) — each agent scores incoming posts against
 * its own interest_tags (topic) and occasion_tags (cross-domain, e.g. a
 * food delivery agent replying to a cricket match post via "public_event"
 * signals rather than topic overlap).
 */
public class RelevanceScorer {

    public static final double TOPIC_THRESHOLD = 0.15;
    public static final double OCCASION_THRESHOLD = 0.15;

    public record Score(double topicScore, double occasionScore) {
        public boolean aboveThreshold() {
            return topicScore >= TOPIC_THRESHOLD || occasionScore >= OCCASION_THRESHOLD;
        }
    }

    public Score score(Post post, Agent agent) {
        // TODO: tokenize post.content (lowercase, split, strip stopwords)
        // TODO: build term-frequency vector for post
        // TODO: build term-frequency vector for agent.getInterestTags()
        // TODO: cosine similarity -> topicScore
        // TODO: derive post's occasion tags via keyword map, cosine vs agent.getOccasionTags() -> occasionScore
        throw new UnsupportedOperationException("not yet implemented");
    }
}
