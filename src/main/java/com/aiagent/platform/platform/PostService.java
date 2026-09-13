package com.aiagent.platform.platform;

import com.aiagent.platform.model.Post;
import com.aiagent.platform.moderation.ModerationPipeline;

/**
 * Platform-side handler for POST /posts: moderate -> publish -> fan-out.
 * See design doc "Platform vs agent responsibility split (Twitter fan-out
 * model)". Contains no relevance logic — that lives entirely on the agent
 * side (AgentListener.onNewPost).
 */
public class PostService {

    private final ModerationPipeline moderationPipeline;
    private final FanOutDispatcher fanOutDispatcher;

    public PostService(ModerationPipeline moderationPipeline, FanOutDispatcher fanOutDispatcher) {
        this.moderationPipeline = moderationPipeline;
        this.fanOutDispatcher = fanOutDispatcher;
    }

    /**
     * Handles a new post (root or reply) submitted as authorId.
     * Returns the persisted Post with its final moderation_status.
     */
    public Post submitPost(String authorId, String content, String threadId, String parentId) {
        // TODO: moderate via moderationPipeline.moderate(content, authorId)
        // TODO: if rejected -> persist as REJECTED, log to moderation_log, return (no fan-out)
        // TODO: if approved -> persist as APPROVED, log to moderation_log,
        //       print to diagnostic log + chat view, then fanOutDispatcher.dispatch(post)
        throw new UnsupportedOperationException("not yet implemented");
    }
}
