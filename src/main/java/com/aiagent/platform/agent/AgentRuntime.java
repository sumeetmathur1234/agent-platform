package com.aiagent.platform.agent;

import com.aiagent.platform.llm.OllamaClient;
import com.aiagent.platform.model.Agent;
import com.aiagent.platform.model.Post;
import com.aiagent.platform.platform.AgentListener;
import com.aiagent.platform.platform.PostService;
import com.aiagent.platform.relevance.RelevanceScorer;

/**
 * Agent-side runtime: wraps an Agent's data with its own decision logic.
 * On receiving a post via the platform's fan-out push, runs loop-control,
 * then relevance scoring, then (if above threshold) generates and submits
 * a reply — all owned here, never on the platform side. See design doc
 * "Platform vs agent responsibility split (Twitter fan-out model)".
 */
public class AgentRuntime implements AgentListener {

    private final Agent agent;
    private final RelevanceScorer relevanceScorer;
    private final OllamaClient ollamaClient;
    private final PostService postService;

    public AgentRuntime(Agent agent, RelevanceScorer relevanceScorer,
                         OllamaClient ollamaClient, PostService postService) {
        this.agent = agent;
        this.relevanceScorer = relevanceScorer;
        this.ollamaClient = ollamaClient;
        this.postService = postService;
    }

    @Override
    public void onNewPost(Post post) {
        // TODO: loop-control check (already replied in post.getThreadId()? past max depth 1?)
        // TODO: if passes, relevanceScorer.score(post, agent); skip if below threshold
        // TODO: if above threshold, ollamaClient.generateReply(agent.getPersona(), post.getContent())
        // TODO: postService.submitPost(agent.getId(), replyText, post.getThreadId(), post.getId())
        throw new UnsupportedOperationException("not yet implemented");
    }
}
