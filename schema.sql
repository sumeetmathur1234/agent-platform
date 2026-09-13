-- AI Agent Social Platform — Postgres schema
-- Matches ai-agent-platform-design.md "Persistence (Postgres)" section exactly.
-- Run manually against a local Postgres instance before starting the app:
--   psql -U <user> -d <database> -f schema.sql

CREATE TABLE IF NOT EXISTS agents (
    id              TEXT PRIMARY KEY,
    persona         TEXT NOT NULL,
    interest_tags   TEXT[] NOT NULL DEFAULT '{}',
    occasion_tags   TEXT[] NOT NULL DEFAULT '{}'
);

CREATE TABLE IF NOT EXISTS posts (
    id                  TEXT PRIMARY KEY,
    author_id           TEXT NOT NULL REFERENCES agents(id),
    content             TEXT NOT NULL,
    thread_id           TEXT NOT NULL,
    parent_id           TEXT REFERENCES posts(id),   -- NULL for root posts
    depth               INT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    moderation_status   TEXT NOT NULL DEFAULT 'pending',  -- pending|approved|rejected
    rejection_reason    TEXT
);
CREATE INDEX IF NOT EXISTS idx_posts_thread ON posts(thread_id);
CREATE INDEX IF NOT EXISTS idx_posts_author_thread ON posts(author_id, thread_id);  -- cooldown lookups

CREATE TABLE IF NOT EXISTS relevance_log (
    id              SERIAL PRIMARY KEY,
    post_id         TEXT NOT NULL REFERENCES posts(id),
    agent_id        TEXT NOT NULL REFERENCES agents(id),
    topic_score     REAL NOT NULL,
    occasion_score  REAL NOT NULL,
    decision        TEXT NOT NULL,  -- replied|skipped
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS moderation_log (
    id          SERIAL PRIMARY KEY,
    post_id     TEXT NOT NULL REFERENCES posts(id),
    verdict     TEXT NOT NULL,  -- approved|rejected
    reason      TEXT,           -- too_long | prompt_injection | toxic | rate_limited | null
    judge_score REAL,           -- null if rejected before reaching the LLM judge
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
