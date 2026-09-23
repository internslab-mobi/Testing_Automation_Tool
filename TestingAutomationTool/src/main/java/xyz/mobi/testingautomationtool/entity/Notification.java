package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "testing_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="assigned_id",nullable = false)
    private User assigned;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bug_id", nullable = false)
    private Bug bug;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status", nullable = false, length = 255)
    private NotificationStatus notificationStatus;
}