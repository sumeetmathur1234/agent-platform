package com.aiagent.platform.moderation;

import com.aiagent.platform.db.BannedWordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BannedWordLearner {

    private final Logger logger = LoggerFactory.getLogger(BannedWordLearner.class);

    @Autowired
    private BannedWordRepository bannedWordRepository;

    @Async
    public void learn(List<String> flaggedTerms) {
        for (String term : flaggedTerms) {
            String normalized = term.trim().toLowerCase();
            if (normalized.isEmpty()) {
                continue;
            }
            try {
                bannedWordRepository.addIfAbsent(normalized, "llm_feedback");
                logger.info("[feedback] learned banned word/phrase from LLM rejection: \"{}\"", normalized);
            } catch (Exception e) {
                logger.error("[feedback] failed to persist banned word \"{}\"", normalized, e);
            }
        }
    }
}
