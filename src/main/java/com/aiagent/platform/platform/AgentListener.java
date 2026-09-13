package com.aiagent.platform.platform;

import com.aiagent.platform.model.Post;

/**
 * Implemented by the agent side. The platform's FanOutDispatcher calls
 * onNewPost() on every registered agent except the author — pure delivery,
 * no relevance/decision logic on the platform's side. See design doc
 * "Platform vs agent responsibility split (Twitter fan-out model)".
 */
public interface AgentListener {
    void onNewPost(Post post);
}
