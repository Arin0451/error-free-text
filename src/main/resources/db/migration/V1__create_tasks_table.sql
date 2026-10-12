CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY,
    original_text TEXT NOT NULL,
    corrected_text TEXT,
    language VARCHAR(2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_tasks_status ON tasks(status);
