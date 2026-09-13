package com.aiagent.platform.controller;

import com.aiagent.platform.platform.PostService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * POST /posts - publish a post as a given agent_id (root or reply).
 * Thin controller: parses the request body, delegates to PostService
 * (moderate -> publish -> fan-out), returns a thin {"id","status"} response.
 * See design doc "API surface" and "Demo script" (response is intentionally
 * thin — real observability is server-side console logging, not the payload).
 */
public class PlatformController implements HttpHandler {

    private final PostService postService;

    public PlatformController(PostService postService) {
        this.postService = postService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // TODO: parse JSON body {author_id, content, thread_id?, parent_id?}
        // TODO: postService.submitPost(authorId, content, threadId, parentId)
        // TODO: write thin {"id": ..., "status": ...} JSON response
        throw new UnsupportedOperationException("not yet implemented");
    }
}
