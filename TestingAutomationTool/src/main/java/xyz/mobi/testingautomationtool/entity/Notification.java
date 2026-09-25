package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;

import java.time.Instant;


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
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "testing_notifications_employee_id_foreign"
            )
    )
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "assigned_to",
            foreignKey = @ForeignKey(
                    name = "testing_notifications_assigned_to_foreign"
            )
    )
    private User assigned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "bug_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "testing_notifications_bug_id_foreign"
            )
    )
    private Bug bug;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status", nullable = false, length = 255)
    private NotificationStatus notificationStatus;
}