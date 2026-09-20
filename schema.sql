CREATE TABLE IF NOT EXISTS agents (
    name            VARCHAR(64) PRIMARY KEY,
    persona         TEXT NOT NULL,
    interest_tags   JSON NOT NULL,   -- e.g. ["cricket", "sports", "match"]
    occasion_tags   JSON NOT NULL    -- e.g. ["public_event", "crowd"]
);

CREATE TABLE IF NOT EXISTS banned_words (
    word        VARCHAR(100) PRIMARY KEY,
    source      VARCHAR(16) NOT NULL DEFAULT 'seed',  -- seed | llm_feedback
    added_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Sample seed list — grows automatically via the LLM feedback loop
-- (ModerationPipeline -> BannedWordLearner) as source='llm_feedback' rows.
INSERT IGNORE INTO banned_words (word, source) VALUES
    ('idiot', 'seed'),
    ('stupid', 'seed'),
    ('shut up', 'seed'),
    ('kill yourself', 'seed'),
    ('scam', 'seed'),
    ('click here now', 'seed'),
    ('free money', 'seed'),
    ('buy followers', 'seed');

CREATE TABLE IF NOT EXISTS follows (
    follower    VARCHAR(64) NOT NULL,  -- agent receiving posts
    followee    VARCHAR(64) NOT NULL,  -- agent being followed (posts flow from here)
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower, followee),
    FOREIGN KEY (follower) REFERENCES agents(name),
    FOREIGN KEY (followee) REFERENCES agents(name)
);
CREATE INDEX idx_follows_followee ON follows(followee);  -- fan-out lookup: who follows this author?

CREATE TABLE IF NOT EXISTS feed_entries (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    agent_name      VARCHAR(64) NOT NULL,  -- whose feed this entry belongs to
    post_id         VARCHAR(64) NOT NULL,
    delivered_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (agent_name) REFERENCES agents(name),
    FOREIGN KEY (post_id) REFERENCES posts(id),
    UNIQUE KEY uq_feed_entry (agent_name, post_id)  -- a post lands in an agent's feed at most once
);
CREATE INDEX idx_feed_entries_agent ON feed_entries(agent_name, delivered_at);  -- feed read query

CREATE TABLE IF NOT EXISTS posts (
    id                  VARCHAR(64) PRIMARY KEY,
    author_id           VARCHAR(64) NOT NULL,
    content             TEXT NOT NULL,
    thread_id           VARCHAR(64) NOT NULL,
    parent_id           VARCHAR(64),   -- NULL for root posts
    depth               INT NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    moderation_status   VARCHAR(16) NOT NULL DEFAULT 'pending',  -- pending|approved|rejected
    rejection_reason    VARCHAR(64),
    FOREIGN KEY (author_id) REFERENCES agents(name),
    FOREIGN KEY (parent_id) REFERENCES posts(id)
);
CREATE INDEX idx_posts_thread ON posts(thread_id);
CREATE INDEX idx_posts_author_thread ON posts(author_id, thread_id);  -- cooldown lookups

CREATE TABLE IF NOT EXISTS relevance_log (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    post_id         VARCHAR(64) NOT NULL,
    agent_id        VARCHAR(64) NOT NULL,
    topic_score     FLOAT NOT NULL,
    occasion_score  FLOAT NOT NULL,
    decision        VARCHAR(16) NOT NULL,  -- replied|skipped
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id),
    FOREIGN KEY (agent_id) REFERENCES agents(name)
);

CREATE TABLE IF NOT EXISTS moderation_log (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    post_id     VARCHAR(64) NOT NULL,
    verdict     VARCHAR(16) NOT NULL,  -- approved|rejected
    reason      VARCHAR(64),           -- too_long | prompt_injection | toxic | rate_limited | null
    judge_score FLOAT,                 -- null if rejected before reaching the LLM judge
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id)
);
