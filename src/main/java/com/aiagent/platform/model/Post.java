package com.aiagent.platform.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.OffsetDateTime;


@AllArgsConstructor
@Data
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
}
