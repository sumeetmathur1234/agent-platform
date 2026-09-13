package com.aiagent.platform.platform;

import com.aiagent.platform.model.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Flat, synchronous, in-process fan-out-on-write push to every registered
 * agent except the author. No queue/broker for the MVP — see design doc
 * "Loop / orchestration control": "Fan-out is the implementation, not
 * just an analogy."
 */
public class FanOutDispatcher {

    private final List<AgentListener> listeners = new ArrayList<>();

    public void register(AgentListener listener) {
        listeners.add(listener);
    }

    /** Pushes the post to every registered listener except the author. */
    public void dispatch(Post post) {
        // TODO: for each listener except post.getAuthorId()'s own agent, call onNewPost(post)
        throw new UnsupportedOperationException("not yet implemented");
    }
}
