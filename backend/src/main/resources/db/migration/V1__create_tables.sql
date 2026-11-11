CREATE TABLE rooms (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    room_code       VARCHAR(10) UNIQUE NOT NULL,
    language        VARCHAR(20) DEFAULT 'javascript',
    max_users       INTEGER DEFAULT 10,
    status          VARCHAR(20) DEFAULT 'ACTIVE',
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    expires_at      TIMESTAMP WITH TIME ZONE,
    created_by      VARCHAR(100)
);

CREATE INDEX idx_rooms_room_code ON rooms (room_code);
CREATE INDEX idx_rooms_status ON rooms (status);

CREATE TABLE documents (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id         UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    content         TEXT DEFAULT '',
    language        VARCHAR(20) DEFAULT 'javascript',
    version         BIGINT DEFAULT 0,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_documents_room_id ON documents (room_id);

CREATE TABLE execution_results (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id         UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    language        VARCHAR(20) NOT NULL,
    code            TEXT NOT NULL,
    output          TEXT,
    error           TEXT,
    exit_code       INTEGER,
    execution_time_ms BIGINT,
    status          VARCHAR(20) DEFAULT 'PENDING',
    submitted_by    VARCHAR(100),
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    completed_at    TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_execution_results_room_id ON execution_results (room_id);
