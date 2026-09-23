package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;
import xyz.mobi.testingautomationtool.enums.ExecutionStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "testing_executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestingExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "execution_id")
    private Integer executionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "testcase_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "testing_executions_testcase_id_foreign"
            )
    )
    private TestCase testCase;

    @Column(name = "bugs_count", nullable = false)
    @Builder.Default
    private Integer bugsCount = 0;

    @Column(name = "execution_number", nullable = false)
    @Builder.Default
    private Integer executionNumber = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "automation_feasibility", length = 255)
    @Builder.Default
    private AutomationFeasibility automationFeasibility =
            AutomationFeasibility.YES;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status",length = 255)
    private ExecutionStatus executionStatus = null;

    @Column(name = "test_execution", columnDefinition = "TEXT")
    private String testExecution;

    @Column(name = "test_validation", columnDefinition = "TEXT")
    private String testValidation;

    @Column(name = "ui_validations", columnDefinition = "TEXT")
    private String uiValidations;

    @Column(name = "db_validations", columnDefinition = "TEXT")
    private String dbValidations;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "precondition", columnDefinition = "TEXT")
    private String precondition;

    @Column(name = "execution_steps", columnDefinition = "TEXT")
    private String executionSteps;

    @Column(name = "test_data", columnDefinition = "TEXT")
    private String testData;

    @Column(name = "executed_at")
    @Builder.Default
    private LocalDateTime executedAt = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by")
    @Builder.Default
    private User executedBy = null;
}