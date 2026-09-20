package com.aiagent.platform.service;

import com.aiagent.platform.db.AgentRepository;
import com.aiagent.platform.model.Agent;
import com.aiagent.platform.model.CreateAgentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class AgentService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AgentRepository agentRepository;

    public Agent createAgent(CreateAgentRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (request.getPersona() == null || request.getPersona().isBlank()) {
            throw new IllegalArgumentException("persona is required");
        }
        if (agentRepository.findByName(request.getName()) != null) {
            throw new IllegalStateException("agent name already exists: " + request.getName());
        }

        Agent agent = new Agent(request.getName(), request.getPersona(), request.getInterestTags(), request.getOccasionTags());
        agentRepository.insert(agent);
        logger.info("created agent name={}", agent.getName());
        return agent;
    }

    public List<Agent> listAgents() {
        List<Agent> agents = agentRepository.findAll();
        logger.info("listed {} agents", agents.size());
        return agents;
    }

    public Agent getAgentByName(String name) {
        Agent agent = agentRepository.findByName(name);
        if (agent == null) {
            throw new IllegalArgumentException("agent not found: " + name);
        }
        return agent;
    }

    public Agent updatePersona(CreateAgentRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (request.getPersona() == null || request.getPersona().isBlank()) {
            throw new IllegalArgumentException("persona is required");
        }
        if (agentRepository.findByName(request.getName()) == null) {
            throw new IllegalArgumentException("agent not found: " + request.getName());
        }

        agentRepository.updatePersona(request.getName(), request.getPersona());
        logger.info("updated persona for agent name={}", request.getName());
        return agentRepository.findByName(request.getName());
    }

    public Agent addInterestTags(String name, List<String> newTags) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (newTags == null || newTags.isEmpty()) {
            throw new IllegalArgumentException("interestTags is required and must not be empty");
        }
        Agent agent = agentRepository.findByName(name);
        if (agent == null) {
            throw new IllegalArgumentException("agent not found: " + name);
        }

        Set<String> merged = new LinkedHashSet<>(agent.getInterestTags());
        merged.addAll(newTags);

        agentRepository.updateInterestTags(name, new ArrayList<>(merged));
        logger.info("added interest tags {} for agent name={}", newTags, name);
        return agentRepository.findByName(name);
    }
}
