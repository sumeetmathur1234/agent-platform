package com.aiagent.platform.agent;

import com.aiagent.platform.config.AppConfig;
import com.aiagent.platform.config.Constants;
import com.aiagent.platform.db.PostRepository;
import com.aiagent.platform.db.RelevanceLogRepository;
import com.aiagent.platform.llm.OllamaClient;
import com.aiagent.platform.model.Agent;
import com.aiagent.platform.model.Post;
import com.aiagent.platform.model.RelevanceLogEntry;
import com.aiagent.platform.platform.AgentListener;
import com.aiagent.platform.platform.PostService;
import com.aiagent.platform.relevance.RelevanceScorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

public class AgentRuntime implements AgentListener {

    private final Logger logger = LoggerFactory.getLogger(AgentRuntime.class);

    private final Agent agent;
    private final RelevanceScorer relevanceScorer;
    private final OllamaClient ollamaClient;
    private final PostService postService;
    private final PostRepository postRepository;
    private final RelevanceLogRepository relevanceLogRepository;

    public AgentRuntime(Agent agent, RelevanceScorer relevanceScorer, OllamaClient ollamaClient,
                         PostService postService, PostRepository postRepository,
                         RelevanceLogRepository relevanceLogRepository) {
        this.agent = agent;
        this.relevanceScorer = relevanceScorer;
        this.ollamaClient = ollamaClient;
        this.postService = postService;
        this.postRepository = postRepository;
        this.relevanceLogRepository = relevanceLogRepository;
    }

    @Override
    public String getAgentName() {
        return agent.getName();
    }

    @Override
    public void onNewPost(Post post) {
        try {
            react(post);
        } catch (Exception e) {
            logger.error("agent={} failed to process post={}", agent.getName(), post.getId(), e);
        }
    }

    private void react(Post post) {
        // Loop-control: already replied in this thread?
        if (postRepository.countByAuthorAndThread(agent.getName(), post.getThreadId()) > 0) {
            logRelevance(post, 0, 0, RelevanceLogEntry.Decision.SKIPPED);
            if (!AppConfig.CHAT_UI_MODE) {
                System.out.println("[relevance] agent=" + agent.getName() + " already replied in thread " + post.getThreadId() + " -> skipped");
            }
            return;
        }

        // Loop-control: past max depth to reply?
        if (post.getDepth() >= Constants.MAX_THREAD_DEPTH) {
            logRelevance(post, 0, 0, RelevanceLogEntry.Decision.SKIPPED);
            if (!AppConfig.CHAT_UI_MODE) {
                System.out.println("[relevance] agent=" + agent.getName() + " post at max depth -> skipped");
            }
            return;
        }

        RelevanceScorer.Score score = relevanceScorer.scoreFactory(post, agent);

        if (!score.aboveThreshold()) {
            logRelevance(post, score.topicScore(), score.occasionScore(), RelevanceLogEntry.Decision.SKIPPED);
            if (!AppConfig.CHAT_UI_MODE) {
                System.out.println("[relevance] agent=" + agent.getName()
                        + " topic=" + round(score.topicScore()) + " occasion=" + round(score.occasionScore())
                        + " -> below threshold, skipped");
            }
            return;
        }

        if (!AppConfig.CHAT_UI_MODE) {
            System.out.println("[relevance] agent=" + agent.getName()
                    + " topic=" + round(score.topicScore()) + " occasion=" + round(score.occasionScore())
                    + " -> above threshold, generating reply");
        }

        String replyText = ollamaClient.generateReply(agent.getPersona(), post.getContent());
        if (!AppConfig.CHAT_UI_MODE) {
            System.out.println("[reply-draft] agent=" + agent.getName() + ": \"" + replyText + "\"");
        }

        logRelevance(post, score.topicScore(), score.occasionScore(), RelevanceLogEntry.Decision.REPLIED);
        postService.submitPost(agent.getName(), replyText, post.getId());
    }

    private void logRelevance(Post post, double topicScore, double occasionScore, RelevanceLogEntry.Decision decision) {
        relevanceLogRepository.insert(new RelevanceLogEntry(0, post.getId(), agent.getName(), topicScore, occasionScore, decision, OffsetDateTime.now()));
    }

    private double round(double value) {
        return Math.round(value * 100) / 100.0;
    }
}
