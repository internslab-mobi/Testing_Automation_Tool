package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "testing_execution_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "execution_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_history_execution")
    )
    private TestingExecution execution;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TestCaseStatus status;

    @Column(name = "changed_by", nullable = false)
    private Integer changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Lob
    @Column(name = "comments")
    private String comments;
}
