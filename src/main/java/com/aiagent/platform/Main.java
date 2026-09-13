package com.aiagent.platform;

import com.aiagent.platform.controller.AgentController;
import com.aiagent.platform.controller.PlatformController;
import com.aiagent.platform.llm.OllamaClient;
import com.aiagent.platform.moderation.ModerationPipeline;
import com.aiagent.platform.platform.FanOutDispatcher;
import com.aiagent.platform.platform.PostService;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

/**
 * Entrypoint: wires Postgres connection, OllamaClient, ModerationPipeline,
 * FanOutDispatcher, PostService, seeds agents, then starts the HTTP server
 * with the 2 controllers. See design doc "Demo script" and "API surface".
 * Hit the endpoints directly via Postman/curl — no separate API wrapper class.
 */
public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        OllamaClient ollamaClient = new OllamaClient();
        ModerationPipeline moderationPipeline = new ModerationPipeline(ollamaClient);
        FanOutDispatcher fanOutDispatcher = new FanOutDispatcher();
        PostService postService = new PostService(moderationPipeline, fanOutDispatcher);

        // TODO: connect to Postgres (schema.sql), seed agents (register AgentRuntime
        // instances with fanOutDispatcher).

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/agents", new AgentController());
        httpServer.createContext("/posts", new PlatformController(postService));
        httpServer.start();

        System.out.println("AI Agent Social Platform listening on port " + PORT);
    }
}
