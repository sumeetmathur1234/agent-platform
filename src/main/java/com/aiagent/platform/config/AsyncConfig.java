package com.aiagent.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "agentReactionExecutor")
    public Executor agentReactionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Constants.AGENT_REACTION_PARALLELISM);
        executor.setMaxPoolSize(Constants.AGENT_REACTION_PARALLELISM);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("agent-reaction-");
        executor.initialize();
        return executor;
    }
}
