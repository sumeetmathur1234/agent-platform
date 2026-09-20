package com.aiagent.platform.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class Follow {
    private final String follower;
    private final String followee;
    private final OffsetDateTime createdAt;
}
