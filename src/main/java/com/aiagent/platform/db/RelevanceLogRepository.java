package com.aiagent.platform.db;

import com.aiagent.platform.model.RelevanceLogEntry;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository
public class RelevanceLogRepository {

    public void insert(RelevanceLogEntry entry) {
        String sql = "INSERT INTO relevance_log (post_id, agent_id, topic_score, occasion_score, decision) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entry.getPostId());
            stmt.setString(2, entry.getAgentId());
            stmt.setDouble(3, entry.getTopicScore());
            stmt.setDouble(4, entry.getOccasionScore());
            stmt.setString(5, entry.getDecision().name().toLowerCase());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert relevance_log entry for post "
                    + entry.getPostId() + " agent " + entry.getAgentId(), e);
        }
    }
}
