package com.aiagent.platform.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Data
public class JudgeResult {
    private final double score;
    private final List<String> flaggedTerms;
}
