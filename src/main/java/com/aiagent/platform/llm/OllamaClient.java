package com.aiagent.platform.llm;

import com.aiagent.platform.config.AppConfig;
import com.aiagent.platform.config.Constants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class OllamaClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public String generateReply(String persona, String postContent) {
        JSONObject body = new JSONObject()
                .put("model", AppConfig.OLLAMA_MODEL)
                .put("system", persona)
                .put("prompt", Constants.replyGenerationPrompt(postContent))
                .put("stream", false);

        JSONObject response = post(body);
        return response.getString("response").trim();
    }

    public JudgeResult judgeContent(String content) {
        JSONObject body = new JSONObject()
                .put("model", AppConfig.OLLAMA_MODEL)
                .put("system", Constants.JUDGE_SYSTEM_PROMPT)
                .put("prompt", Constants.judgeContentPrompt(content))
                .put("format", "json")
                .put("stream", false);

        JSONObject response = post(body);
        JSONObject parsed = new JSONObject(response.getString("response"));

        double score = parsed.optDouble("score", 0.0);
        List<String> flaggedTerms = new ArrayList<>();
        JSONArray terms = parsed.optJSONArray("flagged_terms");
        if (terms != null) {
            for (int i = 0; i < terms.length(); i++) {
                flaggedTerms.add(terms.getString(i));
            }
        }
        return new JudgeResult(score, flaggedTerms);
    }

    public double judgeRelevance(String postContent, String agentPersona, List<String> interestTags) {
        JSONObject body = new JSONObject()
                .put("model", AppConfig.OLLAMA_MODEL)
                .put("system", Constants.RELEVANCE_JUDGE_SYSTEM_PROMPT)
                .put("prompt", Constants.relevanceJudgePrompt(postContent, agentPersona, interestTags))
                .put("format", "json")
                .put("stream", false);

        JSONObject response = post(body);
        JSONObject parsed = new JSONObject(response.getString("response"));
        return parsed.optDouble("relevance_score", 0.0);
    }

    private JSONObject post(JSONObject body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.OLLAMA_BASE_URL + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Ollama returned status " + response.statusCode() + ": " + response.body());
            }
            return new JSONObject(response.body());
        } catch (java.io.IOException | InterruptedException e) {
            throw new RuntimeException("Failed to reach Ollama at " + AppConfig.OLLAMA_BASE_URL
                    + " — is it running? (ollama serve)", e);
        }
    }
}
