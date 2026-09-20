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
public class PostRepository {

    public void insert(Post post) {
        String sql = "INSERT INTO posts (id, author_id, content, thread_id, parent_id, depth, "
                + "moderation_status, rejection_reason) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, post.getId());
            stmt.setString(2, post.getAuthorId());
            stmt.setString(3, post.getContent());
            stmt.setString(4, post.getThreadId());
            stmt.setString(5, post.getParentId());
            stmt.setInt(6, post.getDepth());
            stmt.setString(7, post.getModerationStatus().name().toLowerCase());
            stmt.setString(8, post.getRejectionReason());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert post " + post.getId(), e);
        }
    }

    public Post findById(String id) {
        String sql = "SELECT id, author_id, content, thread_id, parent_id, depth, created_at, "
                + "moderation_status, rejection_reason FROM posts WHERE id = ?";
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch post " + id, e);
        }
    }

    public List<Post> findByThread(String threadId) {
        String sql = "SELECT id, author_id, content, thread_id, parent_id, depth, created_at, "
                + "moderation_status, rejection_reason FROM posts WHERE thread_id = ? ORDER BY created_at";
        List<Post> posts = new ArrayList<>();
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, threadId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch posts for thread " + threadId, e);
        }
        return posts;
    }

    /** Used by AgentRuntime's loop-control cooldown check. */
    public int countByAuthorAndThread(String authorId, String threadId) {
        String sql = "SELECT count(*) FROM posts WHERE author_id = ? AND thread_id = ?";
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authorId);
            stmt.setString(2, threadId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count posts for " + authorId + " in " + threadId, e);
        }
    }

    private Post mapRow(ResultSet rs) throws SQLException {
        OffsetDateTime createdAt = rs.getTimestamp("created_at").toInstant().atOffset(
                OffsetDateTime.now().getOffset());
        String rawStatus = rs.getString("moderation_status");
        Post.ModerationStatus status = Post.ModerationStatus.valueOf(rawStatus.toUpperCase());
        return new Post(
                rs.getString("id"),
                rs.getString("author_id"),
                rs.getString("content"),
                rs.getString("thread_id"),
                rs.getString("parent_id"),
                rs.getInt("depth"),
                createdAt,
                status,
                rs.getString("rejection_reason")
        );
    }
}
