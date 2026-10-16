-- Separate libraries: each book belongs to one library owned by a user.
CREATE TABLE IF NOT EXISTS libraries (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_libraries_user_name UNIQUE (user_id, name)
);
CREATE INDEX IF NOT EXISTS idx_libraries_user_id ON libraries(user_id);

ALTER TABLE books ADD COLUMN IF NOT EXISTS library_id UUID REFERENCES libraries(id) ON DELETE CASCADE;

-- Move existing books into a default library for their owner. The update trigger is
-- paused so the backfill does not rewrite updated_at on every book.
ALTER TABLE books DISABLE TRIGGER books_updated_at;

INSERT INTO libraries (user_id, name)
SELECT DISTINCT user_id, 'My Library' FROM books
ON CONFLICT (user_id, name) DO NOTHING;

UPDATE books b SET library_id = l.id
FROM libraries l
WHERE l.user_id = b.user_id AND l.name = 'My Library' AND b.library_id IS NULL;

ALTER TABLE books ENABLE TRIGGER books_updated_at;

ALTER TABLE books ALTER COLUMN library_id SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_books_library_id ON books(library_id);
