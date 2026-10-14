package xyz.mobi.notificationservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA Entity representing an email notification tracking record.
 * Stores only tracking metadata; never stores email bodies or attachment contents.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
public class Notification {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "sender", length = 255)
    private String sender;

    /**
     * Comma-separated list of primary (TO) recipient email addresses.
     * Storing as a delimited string eliminates multi-table join complexity
     * while remaining clean and fully MySQL-compatible for notification tracking.
     */
    @Column(name = "recipients", length = 2000, nullable = false)
    private String recipients;

    @Column(name = "cc_recipients", length = 2000)
    private String ccRecipients;

    @Column(name = "bcc_recipients", length = 2000)
    private String bccRecipients;

    @Column(name = "subject", length = 500, nullable = false)
    private String subject;

    /**
     * Submission lifecycle status: PENDING, SUBMITTED, FAILED.
     * SUBMITTED represents successful handoff to the SMTP server.
     */
    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;
}
