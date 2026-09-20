package com.aiagent.platform.db;

import com.aiagent.platform.model.Agent;
import org.json.JSONArray;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AgentRepository {

    public void insert(Agent agent) {
        String sql = "INSERT INTO agents (name, persona, interest_tags, occasion_tags) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, agent.getName());
            stmt.setString(2, agent.getPersona());
            stmt.setString(3, new JSONArray(agent.getInterestTags()).toString());
            stmt.setString(4, new JSONArray(agent.getOccasionTags()).toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert agent " + agent.getName(), e);
        }
    }

    public List<Agent> findAll() {
        String sql = "SELECT name, persona, interest_tags, occasion_tags FROM agents";
        List<Agent> agents = new ArrayList<>();
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                agents.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch agents", e);
        }
        return agents;
    }

    public Agent findByName(String name) {
        String sql = "SELECT name, persona, interest_tags, occasion_tags FROM agents WHERE name = ?";
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch agent " + name, e);
        }
    }

    public void updatePersona(String name, String persona) {
        String sql = "UPDATE agents SET persona = ? WHERE name = ?";
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, persona);
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update persona for agent " + name, e);
        }
    }

    public void updateInterestTags(String name, List<String> interestTags) {
        String sql = "UPDATE agents SET interest_tags = ? WHERE name = ?";
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, new JSONArray(interestTags).toString());
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update interest tags for agent " + name, e);
        }
    }

    private Agent mapRow(ResultSet rs) throws SQLException {
        List<String> interestTags = toStringList(new JSONArray(rs.getString("interest_tags")));
        List<String> occasionTags = toStringList(new JSONArray(rs.getString("occasion_tags")));
        return new Agent(rs.getString("name"), rs.getString("persona"), interestTags, occasionTags);
    }

    private List<String> toStringList(JSONArray array) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(array.getString(i));
        }
        return list;
    }
}
