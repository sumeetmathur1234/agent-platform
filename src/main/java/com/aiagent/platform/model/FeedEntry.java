package com.aiagent.platform.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class FeedEntry {
    private final long id;
    private final String agentName;
    private final String postId;
    private final OffsetDateTime deliveredAt;
}
