package com.aiagent.platform.service;

import com.aiagent.platform.db.AgentRepository;
import com.aiagent.platform.db.FollowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FollowService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private AgentRepository agentRepository;

    public void follow(String follower, String followee) {
        if (follower == null || follower.isBlank() || followee == null || followee.isBlank()) {
            throw new IllegalArgumentException("follower and followee are required");
        }
        if (follower.equals(followee)) {
            throw new IllegalArgumentException("an agent cannot follow itself");
        }
        if (agentRepository.findByName(follower) == null) {
            throw new IllegalArgumentException("follower agent not found: " + follower);
        }
        if (agentRepository.findByName(followee) == null) {
            throw new IllegalArgumentException("followee agent not found: " + followee);
        }
        followRepository.insert(follower, followee);
        logger.info("follow created: {} -> {}", follower, followee);
    }

    public List<String> getFollowers(String agentName) {
        List<String> followers = followRepository.findFollowers(agentName);
        logger.info("agent={} has {} follower(s)", agentName, followers.size());
        return followers;
    }

    public List<String> getFollowees(String agentName) {
        List<String> followees = followRepository.findFollowees(agentName);
        logger.info("agent={} follows {} agent(s)", agentName, followees.size());
        return followees;
    }
}
