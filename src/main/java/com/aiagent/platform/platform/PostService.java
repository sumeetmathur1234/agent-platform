package com.aiagent.platform.platform;

import com.aiagent.platform.config.Constants;
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

    public Post submitPost(String authorId, String content, String parentId) {
        logger.info("submitPost authorId={} parentId={}", authorId, parentId);
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
        System.out.println("[moderate] post=" + postId + " author=" + authorId + " verdict=" + status + (verdict.reason() != null ? " reason=" + verdict.reason() : ""));

        if (verdict.approved()) {
            System.out.println("[publish] " + postId + " published, fanning out");
            System.out.println(authorId + ": " + content);
            fanOutDispatcher.dispatch(post);
        } else {
            System.out.println("[blocked] " + postId + " by " + authorId + " rejected, reason=" + verdict.reason() + ", never published");
        }

        return post;
    }
}
