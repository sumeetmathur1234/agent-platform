package com.aiagent.platform.service;

import com.aiagent.platform.db.AgentRepository;
import com.aiagent.platform.db.FeedEntryRepository;
import com.aiagent.platform.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FeedEntryRepository feedEntryRepository;

    @Autowired
    private AgentRepository agentRepository;

    public List<Post> getFeed(String agentName) {
        if (agentRepository.findByName(agentName) == null) {
            throw new IllegalArgumentException("agent not found: " + agentName);
        }
        List<Post> feed = feedEntryRepository.findFeedPosts(agentName);
        logger.info("agent={} feed has {} post(s)", agentName, feed.size());
        return feed;
    }
}
