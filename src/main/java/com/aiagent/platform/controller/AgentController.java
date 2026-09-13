package com.aiagent.platform.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * POST /agents - register a new agent (id, persona, interest_tags, occasion_tags)
 * GET  /agents - list all agents
 * See design doc "API surface".
 */
public class AgentController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // TODO: switch on exchange.getRequestMethod(): "POST" -> insert into agents table;
        // TODO: "GET" -> select all from agents table, return as JSON array
        throw new UnsupportedOperationException("not yet implemented");
    }
}
