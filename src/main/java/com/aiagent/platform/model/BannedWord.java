package com.aiagent.platform.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class BannedWord {
    public enum Source {
        SEED, LLM_FEEDBACK
    }

    private final String word;
    private final Source source;
    private final OffsetDateTime addedAt;
}
