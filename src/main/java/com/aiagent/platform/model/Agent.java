package com.aiagent.platform.model;

import java.util.List;

/**
 * Agent: id, persona (used as the Ollama system prompt), interest/occasion tags
 * (used by the agent's own RelevanceScorer). See ai-agent-platform-design.md
 * "Data model" and "Relevance logic (agent side)".
 */
public class Agent {

    private final String id;
    private final String persona;
    private final List<String> interestTags;
    private final List<String> occasionTags;

    public Agent(String id, String persona, List<String> interestTags, List<String> occasionTags) {
        this.id = id;
        this.persona = persona;
        this.interestTags = interestTags;
        this.occasionTags = occasionTags;
    }

    public String getId() {
        return id;
    }

    public String getPersona() {
        return persona;
    }

    public List<String> getInterestTags() {
        return interestTags;
    }

    public List<String> getOccasionTags() {
        return occasionTags;
    }
}
