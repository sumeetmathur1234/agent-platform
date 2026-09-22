package com.aiagent.platform.controller;

import com.aiagent.platform.model.CreatePostRequest;
import com.aiagent.platform.model.ErrorResponse;
import com.aiagent.platform.model.Post;
import com.aiagent.platform.platform.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/platform")
public class PlatformController {

    private final Logger logger = LoggerFactory.getLogger(PlatformController.class);

    @Autowired
    private PostService postService;

    @PostMapping("/createPost")
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequest request) {
        logger.info("POST /platform/createPost authorId={} parentId={}", request.getAuthorId(), request.getParentId());
        try {
            Post post = postService.submitPost(request.getAuthorId(), request.getContent(), request.getParentId());
            HttpStatus status = post.getModerationStatus() == Post.ModerationStatus.REJECTED
                    ? HttpStatus.UNPROCESSABLE_ENTITY
                    : HttpStatus.CREATED;
            return ResponseEntity.status(status).body(post);
        } catch (IllegalArgumentException e) {
            logger.warn("createPost rejected: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("createPost failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorResponse(e.getMessage()));
        }
    }
}
