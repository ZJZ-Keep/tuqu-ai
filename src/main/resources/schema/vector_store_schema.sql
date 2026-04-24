CREATE TABLE IF NOT EXISTS vector_store (
    id VARCHAR(36) PRIMARY KEY,
    content TEXT NOT NULL,
    metadata JSONB,
    embedding VECTOR(1024)
);

CREATE INDEX IF NOT EXISTS vector_store_embedding_idx ON vector_store USING hnsw (embedding vector_cosine_ops);


select  * from vector_store;