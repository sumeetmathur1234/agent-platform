package com.aiagent.platform.db;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FollowRepository {

    public void insert(String follower, String followee) {
        String sql = "INSERT IGNORE INTO follows (follower, followee) VALUES (?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, follower);
            stmt.setString(2, followee);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert follow " + follower + " -> " + followee, e);
        }
    }

    /** Names of every agent that follows the given followee — used by fan-out. */
    public List<String> findFollowers(String followee) {
        String sql = "SELECT follower FROM follows WHERE followee = ?";
        List<String> followers = new ArrayList<>();
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, followee);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    followers.add(rs.getString("follower"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch followers of " + followee, e);
        }
        return followers;
    }

    /** Names of every agent the given follower follows. */
    public List<String> findFollowees(String follower) {
        String sql = "SELECT followee FROM follows WHERE follower = ?";
        List<String> followees = new ArrayList<>();
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, follower);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    followees.add(rs.getString("followee"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch followees of " + follower, e);
        }
        return followees;
    }
}
