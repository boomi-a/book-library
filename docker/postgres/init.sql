-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role          VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Libraries: each user can keep separate libraries (e.g. their own books and another person's)
CREATE TABLE IF NOT EXISTS libraries (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_libraries_user_name UNIQUE (user_id, name)
);
CREATE INDEX IF NOT EXISTS idx_libraries_user_id ON libraries(user_id);

-- Books table
CREATE TABLE IF NOT EXISTS books (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    library_id  UUID NOT NULL REFERENCES libraries(id) ON DELETE CASCADE,
    book_name   VARCHAR(500) NOT NULL,
    author      VARCHAR(255),
    publisher   VARCHAR(255),
    category    VARCHAR(100),
    bought_at   TIMESTAMPTZ,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    comments    TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for fast per-user lookups and filtering
CREATE INDEX IF NOT EXISTS idx_books_user_id  ON books(user_id);
CREATE INDEX IF NOT EXISTS idx_books_library_id ON books(library_id);
CREATE INDEX IF NOT EXISTS idx_books_category ON books(category);
CREATE INDEX IF NOT EXISTS idx_books_is_read  ON books(is_read);

-- Auto-update updated_at on any row change
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS books_updated_at ON books;
CREATE TRIGGER books_updated_at
    BEFORE UPDATE ON books
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Seed default ADMIN user
-- Password: Admin@123  (BCrypt $2a$12$ hash — change in production)
INSERT INTO users (email, username, password_hash, role)
VALUES (
    'admin@booklibrary.com',
    'admin',
    '$2y$12$dhKvi.ogXJQQd5uCJ1eNkOmH7dKbvbrYnzcXMRQ3K7f1XG4yjNj3O',
    'ADMIN'
)
ON CONFLICT (email) DO NOTHING;
