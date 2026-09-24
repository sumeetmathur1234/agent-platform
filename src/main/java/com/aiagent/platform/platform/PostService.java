package com.aiagent.platform.platform;

import com.aiagent.platform.config.AppConfig;
import com.aiagent.platform.config.Constants;
import com.aiagent.platform.db.AgentRepository;
import com.aiagent.platform.db.ModerationLogRepository;
import com.aiagent.platform.db.PostRepository;
import com.aiagent.platform.model.ModerationLogEntry;
import com.aiagent.platform.model.Post;
import com.aiagent.platform.moderation.ModerationPipeline;
import com.aiagent.platform.moderation.ModerationVerdict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PostService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ModerationPipeline moderationPipeline;

    @Autowired
    private FanOutDispatcher fanOutDispatcher;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ModerationLogRepository moderationLogRepository;

    @Autowired
    private AgentRepository agentRepository;

    public Post submitPost(String authorId, String content, String parentId) {
        logger.info("submitPost authorId={} parentId={}", authorId, parentId);
        if (agentRepository.findByName(authorId) == null) {
            logger.warn("submitPost rejected: author not found: {}", authorId);
            throw new IllegalArgumentException("author not found: " + authorId);
        }

        OffsetDateTime windowStart = OffsetDateTime.now().minusSeconds(Constants.RATE_LIMIT_WINDOW_SECONDS);
        int recentPosts = postRepository.countByAuthorSince(authorId, windowStart);
        if (recentPosts >= Constants.RATE_LIMIT_MAX_POSTS) {
            logger.warn("submitPost rejected: rate limit exceeded for author={} ({} posts in last {}s)",
                    authorId, recentPosts, Constants.RATE_LIMIT_WINDOW_SECONDS);
            throw new IllegalArgumentException("rate limit exceeded: max " + Constants.RATE_LIMIT_MAX_POSTS
                    + " posts per " + Constants.RATE_LIMIT_WINDOW_SECONDS + "s");
        }

        String threadId;
        int depth;

        if (parentId == null) {
            //root post
            threadId = UUID.randomUUID().toString();
            depth = 0;
        } else {
            //reply
            Post parent = postRepository.findById(parentId);
            if (parent == null) {
                logger.error("submitPost rejected: parent post not found: {}", parentId);
                throw new IllegalArgumentException("parent post not found: " + parentId);
            }
            if (parent.getDepth() >= Constants.MAX_THREAD_DEPTH) {
                logger.error("submitPost rejected: max thread depth ({}) exceeded for parent={}", Constants.MAX_THREAD_DEPTH, parentId);
                throw new IllegalArgumentException("max thread depth (" + Constants.MAX_THREAD_DEPTH + ") exceeded");
            }
            threadId = parent.getThreadId();
            depth = parent.getDepth() + 1;
        }

        ModerationVerdict verdict = moderationPipeline.moderate(content, authorId);

        String postId = UUID.randomUUID().toString();
        Post.ModerationStatus status = verdict.approved() ? Post.ModerationStatus.APPROVED : Post.ModerationStatus.REJECTED;
        Post post = new Post(postId, authorId, content, threadId, parentId, depth, OffsetDateTime.now(), status, verdict.reason());

        postRepository.insert(post);

        moderationLogRepository.insert(new ModerationLogEntry(0, postId, status, verdict.reason(), verdict.judgeScore(), OffsetDateTime.now()));

        logger.info("moderation verdict post={} author={} status={} reason={}", postId, authorId, status, verdict.reason());
        if (!AppConfig.CHAT_UI_MODE) {
            System.out.println("[moderate] post=" + postId + " author=" + authorId + " verdict=" + status + (verdict.reason() != null ? " reason=" + verdict.reason() : ""));
        }

        if (verdict.approved()) {
            if (!AppConfig.CHAT_UI_MODE) {
                System.out.println("[publish] " + postId + " published, fanning out");
            }
            printChatLine(post);
            fanOutDispatcher.dispatch(post);
        } else if (!AppConfig.CHAT_UI_MODE) {
            System.out.println("[blocked] " + postId + " by " + authorId + " rejected, reason=" + verdict.reason() + ", never published");
        }

        return post;
    }

    // ponytail: demo-only console formatting, not a real chat UI — prints
    // one clean "@agent [-> replying to @parent]: content" line per
    // published post so a live demo reads like a conversation feed instead
    // of interleaved diagnostic log lines. Always prints (chat mode or
    // not) since it's the one line meant to be visible either way.
    // Upgrade path if this needs to be more than a terminal demo aid: a
    // proper SSE/WebSocket feed endpoint instead of System.out.
    private void printChatLine(Post post) {
        String prefix = post.getParentId() == null ? "" : "\u21B3 "; // reply indent arrow
        System.out.println(">>> " + prefix + "@" + post.getAuthorId() + ": " + post.getContent());
    }
}
