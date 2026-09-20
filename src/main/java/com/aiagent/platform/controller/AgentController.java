package com.aiagent.platform.controller;

import com.aiagent.platform.model.Agent;
import com.aiagent.platform.model.CreateAgentRequest;
import com.aiagent.platform.model.CreateFollowRequest;
import com.aiagent.platform.model.ErrorResponse;
import com.aiagent.platform.service.AgentService;
import com.aiagent.platform.service.FeedService;
import com.aiagent.platform.service.FollowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final Logger logger = LoggerFactory.getLogger(AgentController.class);

    @Autowired
    private AgentService agentService;

    @Autowired
    private FollowService followService;

    @Autowired
    private FeedService feedService;

    @PostMapping("/createAgent")
    public ResponseEntity<?> createAgent(@RequestBody CreateAgentRequest request) {
        logger.info("POST /agent/createAgent name={}", request.getName());
        try {
            Agent agent = agentService.createAgent(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(agent);
        } catch (IllegalArgumentException e) {
            logger.warn("createAgent rejected: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("createAgent conflict: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/getAllAgents")
    public List<Agent> listAgents() {
        logger.info("GET /agent/getAllAgents");
        return agentService.listAgents();
    }

    @PostMapping("/updatePersona")
    public ResponseEntity<?> updatePersona(@RequestBody CreateAgentRequest request) {
        logger.info("POST /agent/updatePersona name={}", request.getName());
        try {
            Agent agent = agentService.updatePersona(request);
            return ResponseEntity.ok(agent);
        } catch (IllegalArgumentException e) {
            logger.warn("updatePersona rejected: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/addInterestTags")
    public ResponseEntity<?> addInterestTags(@RequestBody CreateAgentRequest request) {
        logger.info("POST /agent/addInterestTags name={} tags={}", request.getName(), request.getInterestTags());
        try {
            Agent agent = agentService.addInterestTags(request.getName(), request.getInterestTags());
            return ResponseEntity.ok(agent);
        } catch (IllegalArgumentException e) {
            logger.warn("addInterestTags rejected: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/getAgent")
    public ResponseEntity<?> getAgent(@RequestParam String name) {
        logger.info("GET /agent/getAgent name={}", name);
        try {
            return ResponseEntity.ok(agentService.getAgentByName(name));
        } catch (IllegalArgumentException e) {
            logger.warn("getAgent not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/follow")
    public ResponseEntity<?> follow(@RequestBody CreateFollowRequest request) {
        logger.info("POST /agent/follow follower={} followee={}", request.getFollower(), request.getFollowee());
        try {
            followService.follow(request.getFollower(), request.getFollowee());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            logger.warn("follow rejected: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/followers")
    public List<String> getFollowers(@RequestParam String name) {
        logger.info("GET /agent/followers name={}", name);
        return followService.getFollowers(name);
    }

    @GetMapping("/followees")
    public List<String> getFollowees(@RequestParam String name) {
        logger.info("GET /agent/followees name={}", name);
        return followService.getFollowees(name);
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(@RequestParam String name) {
        logger.info("GET /agent/feed name={}", name);
        try {
            return ResponseEntity.ok(feedService.getFeed(name));
        } catch (IllegalArgumentException e) {
            logger.warn("getFeed not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }
}
