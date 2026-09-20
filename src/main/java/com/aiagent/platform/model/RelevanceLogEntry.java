package com.aiagent.platform.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class RelevanceLogEntry {

    public enum Decision {
        REPLIED, SKIPPED
    }

    private final long id;
    private final String postId;
    private final String agentId;
    private final double topicScore;
    private final double occasionScore;
    private final Decision decision;
    private final OffsetDateTime createdAt;
}
