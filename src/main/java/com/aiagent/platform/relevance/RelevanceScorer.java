package com.aiagent.platform.relevance;

import com.aiagent.platform.config.Constants;
import com.aiagent.platform.model.Agent;
import com.aiagent.platform.model.Post;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RelevanceScorer {

    public record Score(double topicScore, double occasionScore) {
        public boolean aboveThreshold() {
            return topicScore >= Constants.TOPIC_THRESHOLD || occasionScore >= Constants.OCCASION_THRESHOLD;
        }
    }

    public Score score(Post post, Agent agent) {
        List<String> postTokens = tokenize(post.getContent());
        Map<String, Integer> postVector = termFrequency(postTokens);

        Map<String, Integer> interestVector = termFrequency(agent.getInterestTags());
        double topicScore = cosineSimilarity(postVector, interestVector);

        List<String> postOccasionTags = deriveOccasionTags(postTokens);
        Map<String, Integer> postOccasionVector = termFrequency(postOccasionTags);
        Map<String, Integer> agentOccasionVector = termFrequency(agent.getOccasionTags());
        double occasionScore = cosineSimilarity(postOccasionVector, agentOccasionVector);

        return new Score(topicScore, occasionScore);
    }

    private List<String> tokenize(String text) {
        return List.of(text.toLowerCase().split("\\W+")).stream()
                .filter(token -> !token.isBlank() && !Constants.STOPWORDS.contains(token))
                .toList();
    }

    private List<String> deriveOccasionTags(List<String> postTokens) {
        return postTokens.stream()
                .filter(Constants.OCCASION_KEYWORDS::containsKey)
                .flatMap(token -> Constants.OCCASION_KEYWORDS.get(token).stream())
                .toList();
    }

    private Map<String, Integer> termFrequency(List<String> terms) {
        Map<String, Integer> vector = new HashMap<>();
        for (String term : terms) {
            vector.merge(term.toLowerCase(), 1, Integer::sum);
        }
        return vector;
    }

    private double cosineSimilarity(Map<String, Integer> a, Map<String, Integer> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        double dot = 0;
        for (Map.Entry<String, Integer> entry : a.entrySet()) {
            Integer bVal = b.get(entry.getKey());
            if (bVal != null) {
                dot += entry.getValue() * bVal;
            }
        }
        double normA = Math.sqrt(a.values().stream().mapToDouble(v -> (double) v * v).sum());
        double normB = Math.sqrt(b.values().stream().mapToDouble(v -> (double) v * v).sum());
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dot / (normA * normB);
    }
}
