package com.aiagent.platform.db;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BannedWordRepository {

    public List<String> findAllWords() {
        String sql = "SELECT word FROM banned_words";
        List<String> words = new ArrayList<>();
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                words.add(rs.getString("word"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch banned words", e);
        }
        return words;
    }

    public void addIfAbsent(String word, String source) {
        String sql = "INSERT IGNORE INTO banned_words (word, source) VALUES (?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, word);
            stmt.setString(2, source);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add banned word " + word, e);
        }
    }

    public int countBySource(String source) {
        String sql = "SELECT count(*) FROM banned_words WHERE source = ?";
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, source);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count banned words for source " + source, e);
        }
    }
}
