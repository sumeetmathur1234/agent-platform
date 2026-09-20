package com.aiagent.platform.db;

import com.aiagent.platform.model.ModerationLogEntry;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

@Repository
public class ModerationLogRepository {

    public void insert(ModerationLogEntry entry) {
        String sql = "INSERT INTO moderation_log (post_id, verdict, reason, judge_score) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entry.getPostId());
            stmt.setString(2, entry.getVerdict().name().toLowerCase());
            stmt.setString(3, entry.getReason());
            if (entry.getJudgeScore() != null) {
                stmt.setDouble(4, entry.getJudgeScore());
            } else {
                stmt.setNull(4, Types.FLOAT);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert moderation_log entry for post " + entry.getPostId(), e);
        }
    }
}
