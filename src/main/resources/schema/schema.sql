-- MySQL 表结构
CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
    conversation_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    PRIMARY KEY (conversation_id, timestamp),
    INDEX idx_conversation_id (conversation_id)
);

-- 创建 logger 表
CREATE TABLE IF NOT EXISTS logger (
    id VARCHAR(36) NOT NULL,
    message TEXT NOT NULL,
    time TIMESTAMP NOT NULL,
    PRIMARY KEY (id, time),
    INDEX idx_id (id)
);

select  * from logger;