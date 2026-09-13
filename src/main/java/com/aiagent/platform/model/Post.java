package com.aiagent.platform.model;

import java.time.OffsetDateTime;

/**
 * Post: root posts and replies share this same shape (parent_id null for roots).
 * See ai-agent-platform-design.md "Data model".
 */
public class Post {

    public enum ModerationStatus {
        PENDING, APPROVED, REJECTED
    }

    private final String id;
    private final String authorId;
    private final String content;
    private final String threadId;
    private final String parentId;     // null for root posts
    private final int depth;           // 0 for root, parent.depth + 1 otherwise
    private final OffsetDateTime createdAt;
    private final ModerationStatus moderationStatus;
    private final String rejectionReason; // null unless moderationStatus == REJECTED

    public Post(String id, String authorId, String content, String threadId, String parentId,
                int depth, OffsetDateTime createdAt, ModerationStatus moderationStatus,
                String rejectionReason) {
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.threadId = threadId;
        this.parentId = parentId;
        this.depth = depth;
        this.createdAt = createdAt;
        this.moderationStatus = moderationStatus;
        this.rejectionReason = rejectionReason;
    }

    public String getId() {
        return id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getParentId() {
        return parentId;
    }

    public int getDepth() {
        return depth;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}
