package com.aiagent.platform.platform;

import com.aiagent.platform.agent.AgentRuntime;
import com.aiagent.platform.db.PostRepository;
import com.aiagent.platform.db.RelevanceLogRepository;
import com.aiagent.platform.llm.OllamaClient;
import com.aiagent.platform.model.Agent;
import com.aiagent.platform.model.Post;
import com.aiagent.platform.relevance.RelevanceScorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AgentReactionDispatcher {

    private final Logger logger = LoggerFactory.getLogger(AgentReactionDispatcher.class);

    @Autowired
    private RelevanceScorer relevanceScorer;

    @Autowired
    private OllamaClient ollamaClient;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private RelevanceLogRepository relevanceLogRepository;

    @Autowired
    @Lazy
    private PostService postService;

    @Async("agentReactionExecutor")
    public void react(Agent followerAgent, Post post) {
        try {
            AgentListener runtime = new AgentRuntime(followerAgent, relevanceScorer, ollamaClient, postService, postRepository, relevanceLogRepository);
            runtime.onNewPost(post);
        } catch (Exception e) {
            logger.error("agent={} async reaction failed for post={}", followerAgent.getName(), post.getId(), e);
        }
    }
}
