package com.aiagent.platform.platform;

import com.aiagent.platform.model.Post;

public interface AgentListener {
    String getAgentName();

    void onNewPost(Post post);
}
