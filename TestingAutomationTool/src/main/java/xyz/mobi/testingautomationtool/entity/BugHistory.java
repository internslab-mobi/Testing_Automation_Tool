package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "testing_bug_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bug_log_id")
    private Integer bugLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "bug_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "testing_bug_history_bug_id_foreign"
            )
    )
    private Bug bug;

    @Column(name = "executed_by", nullable = false)
    private Integer executedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "bug_status", nullable = false, length = 255)
    private BugStatus bugStatus;

    @Column(name = "assigned_to")
    private Integer assignedTo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}