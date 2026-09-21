package com.aiagent.platform.relevance;

import com.aiagent.platform.config.AppConfig;
import com.aiagent.platform.config.Constants;
import com.aiagent.platform.llm.OllamaClient;
import com.aiagent.platform.model.Agent;
import com.aiagent.platform.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tartarus.snowball.ext.PorterStemmer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RelevanceScorer {

    private final Logger logger = LoggerFactory.getLogger(RelevanceScorer.class);

    @Autowired
    private OllamaClient ollamaClient;

    public record Score(double topicScore, double occasionScore) {
        public boolean aboveThreshold() {
            return topicScore >= Constants.TOPIC_THRESHOLD || occasionScore >= Constants.OCCASION_THRESHOLD;
        }
    }


    public Score scoreFactory(Post post, Agent agent) {
        switch (AppConfig.RELEVANCE_SCORER_VERSION) {
            case "v2":
                return scoreV2(post, agent);
            case "v3":
                return scoreV3(post, agent);
            case "v1":
                return score(post, agent);
            default:
                logger.warn("unrecognized RELEVANCE_SCORER_VERSION=\"{}\", falling back to v1", AppConfig.RELEVANCE_SCORER_VERSION);
                return score(post, agent);
        }
    }

    //pure cosine matching
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

    //pure llm based logic
    public Score scoreV2(Post post, Agent agent) {
        double relevance = ollamaClient.judgeRelevance(post.getContent(), agent.getPersona(), agent.getInterestTags());
        return new Score(relevance, relevance);
    }

    public Score scoreV3(Post post, Agent agent) {
        List<String> postTokens = stemAndNormalize(tokenize(post.getContent()));
        Map<String, Integer> postVector = termFrequency(postTokens);

        List<String> interestTokens = stemAndNormalize(agent.getInterestTags());
        Map<String, Integer> interestVector = termFrequency(interestTokens);

        double topicScore = weightedCosineSimilarity(postVector, interestVector);

        List<String> postOccasionTags = deriveOccasionTags(postTokens);
        Map<String, Integer> postOccasionVector = termFrequency(postOccasionTags);
        Map<String, Integer> agentOccasionVector = termFrequency(agent.getOccasionTags());
        double occasionScore = weightedCosineSimilarity(postOccasionVector, agentOccasionVector);

        return new Score(topicScore, occasionScore);
    }

    private List<String> stemAndNormalize(List<String> tokens) {
        return tokens.stream()
                .map(String::toLowerCase)
                .map(this::stem)
                .toList();
    }

    private String stem(String token) {
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(token);
        stemmer.stem();
        return stemmer.getCurrent();
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

    private double weightedCosineSimilarity(Map<String, Integer> a, Map<String, Integer> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        double dot = 0;
        for (Map.Entry<String, Integer> entry : a.entrySet()) {
            Integer bVal = b.get(entry.getKey());
            if (bVal != null) {
                double idfWeight = 1.0 / (1 + Math.log(1 + b.size()));
                dot += entry.getValue() * bVal * idfWeight;
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
