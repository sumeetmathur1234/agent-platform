package com.aiagent.platform.platform;

import com.aiagent.platform.db.AgentRepository;
import com.aiagent.platform.db.FeedEntryRepository;
import com.aiagent.platform.db.FollowRepository;
import com.aiagent.platform.model.Agent;
import com.aiagent.platform.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FanOutDispatcher {

    private final Logger logger = LoggerFactory.getLogger(FanOutDispatcher.class);

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private FeedEntryRepository feedEntryRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AgentReactionDispatcher agentReactionDispatcher;

    /** Writes a feed entry for every follower, then fires each one's reaction in parallel. Runs off the request thread. */
    @Async
    public void dispatch(Post post) {
        List<String> followers = followRepository.findFollowers(post.getAuthorId());
        logger.info("dispatching post={} author={} to {} follower(s): {}",
                post.getId(), post.getAuthorId(), followers.size(), followers);
        for (String followerName : followers) {
            feedEntryRepository.insert(followerName, post.getId());

            Agent followerAgent = agentRepository.findByName(followerName);
            if (followerAgent == null) {
                continue; // shouldn't happen (FK-enforced), defensive skip
            }
            agentReactionDispatcher.react(followerAgent, post);
        }
    }
}
