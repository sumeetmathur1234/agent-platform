package com.aiagent.platform.moderation;

import com.aiagent.platform.config.Constants;
import com.aiagent.platform.db.BannedWordRepository;
import com.aiagent.platform.llm.JudgeResult;
import com.aiagent.platform.llm.OllamaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModerationPipeline {

    private final Logger logger = LoggerFactory.getLogger(ModerationPipeline.class);

    @Autowired
    private OllamaClient ollamaClient;

    @Autowired
    private BannedWordRepository bannedWordRepository;

    @Autowired
    private BannedWordLearner bannedWordLearner;

    public ModerationVerdict moderate(String content, String authorId) {
        String lower = content.toLowerCase();

        // 1. rule-based filters
        if (content.length() > Constants.MAX_POST_LENGTH) {
            return ModerationVerdict.rejected("too_long");
        }
        for (String banned : bannedWordRepository.findAllWords()) {
            if (lower.contains(banned.toLowerCase())) {
                return ModerationVerdict.rejected("banned_word");
            }
        }

        // 2. prompt-injection regex (substring match, case-insensitive)
        for (String marker : Constants.INJECTION_MARKERS) {
            if (lower.contains(marker.toLowerCase())) {
                return ModerationVerdict.rejected("prompt_injection");
            }
        }

        JudgeResult judgeResult;
        try {
            long start = System.currentTimeMillis();
            judgeResult = ollamaClient.judgeContent(content);
            long elapsedMs = System.currentTimeMillis() - start;
            logger.info("ollama judge score={} flaggedTerms={} elapsedMs={}", judgeResult.getScore(), judgeResult.getFlaggedTerms(), elapsedMs);
        } catch (RuntimeException e) {
            logger.warn("ollama judge unreachable, approving based on rule-based checks only: {}", e.getMessage());
            return ModerationVerdict.approved(null);
        }

        if (judgeResult.getScore() > Constants.JUDGE_REJECT_THRESHOLD) {
            //feedback loop to improve the banned words list
            bannedWordLearner.learn(judgeResult.getFlaggedTerms());
            return ModerationVerdict.rejected("toxic", judgeResult.getScore());
        }

        return ModerationVerdict.approved(judgeResult.getScore());
    }
}
