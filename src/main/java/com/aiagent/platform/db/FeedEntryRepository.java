package com.aiagent.platform.db;

import com.aiagent.platform.model.Post;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FeedEntryRepository {

    public void insert(String agentName, String postId) {
        String sql = "INSERT IGNORE INTO feed_entries (agent_name, post_id) VALUES (?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, agentName);
            stmt.setString(2, postId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert feed entry for " + agentName + "/" + postId, e);
        }
    }

    /** Full posts in an agent's feed, most recent first. */
    public List<Post> findFeedPosts(String agentName) {
        String sql = "SELECT p.id, p.author_id, p.content, p.thread_id, p.parent_id, p.depth, "
                + "p.created_at, p.moderation_status, p.rejection_reason "
                + "FROM feed_entries f JOIN posts p ON f.post_id = p.id "
                + "WHERE f.agent_name = ? ORDER BY f.delivered_at DESC";
        List<Post> posts = new ArrayList<>();
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, agentName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OffsetDateTime createdAt = rs.getTimestamp("created_at").toInstant()
                            .atOffset(OffsetDateTime.now().getOffset());
                    Post.ModerationStatus status = Post.ModerationStatus.valueOf(
                            rs.getString("moderation_status").toUpperCase());
                    posts.add(new Post(
                            rs.getString("id"), rs.getString("author_id"), rs.getString("content"),
                            rs.getString("thread_id"), rs.getString("parent_id"), rs.getInt("depth"),
                            createdAt, status, rs.getString("rejection_reason")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch feed for " + agentName, e);
        }
        return posts;
    }
}
