-- ===================================================================
-- Email Notification Microservice - Database Schema DDL
-- Target: Dedicated MySQL Database (e.g., notification_service_db)
-- Note: Do NOT execute in Interns_db or shared company databases.
-- ===================================================================

CREATE DATABASE IF NOT EXISTS notification_service_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE Interns_db;

CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(36) NOT NULL,
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

