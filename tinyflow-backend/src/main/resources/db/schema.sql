-- TinyFlow Database Schema
-- MySQL 8.0+

-- Create database
CREATE DATABASE IF NOT EXISTS tinyflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tinyflow;

-- Thread table: stores conversation metadata
CREATE TABLE IF NOT EXISTS tf_thread (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    thread_id VARCHAR(64) NOT NULL COMMENT '会话唯一标识',
    title VARCHAR(255) DEFAULT '新对话' COMMENT '会话标题',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    status TINYINT DEFAULT 0 COMMENT '状态：0-活跃 1-暂停等待 2-完成',
    current_checkpoint_id VARCHAR(128) DEFAULT NULL COMMENT '当前checkpoint ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_thread_id (thread_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- Message table: stores conversation messages
CREATE TABLE IF NOT EXISTS tf_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    thread_id VARCHAR(64) NOT NULL COMMENT '所属会话ID',
    role VARCHAR(32) NOT NULL COMMENT '角色：user/assistant/system/tool',
    content TEXT NOT NULL COMMENT '消息内容',
    metadata JSON DEFAULT NULL COMMENT '元数据（模型、token等）',
    sequence INT DEFAULT 0 COMMENT '消息顺序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_thread_id (thread_id),
    INDEX idx_sequence (thread_id, sequence),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- Initialize data (optional)
-- INSERT INTO tf_thread (thread_id, title, status) VALUES ('default', '默认会话', 0);
