package com.aiagent.platform.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.OffsetDateTime;


@Data
@AllArgsConstructor
public class ModerationLogEntry {

    private final long id;
    private final String postId;
    private final Post.ModerationStatus verdict; // APPROVED or REJECTED (never PENDING here)
    private final String reason;                  // null if approved
    private final Double judgeScore;                // null if rejected before reaching the LLM judge
    private final OffsetDateTime createdAt;
}
