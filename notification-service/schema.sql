-- Email Notification Microservice
-- Target database: Interns_db

USE Interns_db;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT NOT NULL,
    sender VARCHAR(255) NULL,
    recipients VARCHAR(2000) NOT NULL,
    cc_recipients VARCHAR(2000) NULL,
    bcc_recipients VARCHAR(2000) NULL,
    subject VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL,
    failure_reason VARCHAR(1000) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    submitted_at TIMESTAMP(6) NULL,

    PRIMARY KEY (id),

    INDEX idx_notifications_status (status),
    INDEX idx_notifications_created_at (created_at)

    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Safe Migration Script for existing Interns_db database (Preserves records)
-- ============================================================================
-- If updating an existing table that contains UUID VARCHAR(36) records:
--
-- 1. Add new auto-increment ID column:
--    ALTER TABLE notifications ADD COLUMN new_id BIGINT AUTO_INCREMENT UNIQUE FIRST;
--
-- 2. Switch primary key to new auto-increment column:
--    ALTER TABLE notifications DROP PRIMARY KEY;
--    ALTER TABLE notifications DROP COLUMN id;
--    ALTER TABLE notifications CHANGE COLUMN new_id id BIGINT AUTO_INCREMENT PRIMARY KEY;
-- ============================================================================