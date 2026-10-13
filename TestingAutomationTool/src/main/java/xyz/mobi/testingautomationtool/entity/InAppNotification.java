package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.InAppNotificationStatus;

import java.time.Instant;

@Entity
@Table(name = "testing_in_app_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InAppNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_in_app_notification_employee")
    )
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "bug_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_in_app_notification_bug")
    )
    private Bug bug;

    @Column(name = "message", nullable = false, length = 255)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "notification_status",
            nullable = false,
            length = 50
    )
    @Builder.Default
    private InAppNotificationStatus notificationStatus = InAppNotificationStatus.UNREAD;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}