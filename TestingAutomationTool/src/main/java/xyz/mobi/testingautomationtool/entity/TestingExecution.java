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
    private Integer bugsCount;

    @Column(name = "execution_number", nullable = false)
    private Integer executionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "automation_feasibility", length = 255)
    private AutomationFeasibility automationFeasibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false, length = 255)
    private ExecutionStatus executionStatus;

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
    private LocalDateTime executedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "executed_by",
            nullable = false
    )
    private User executedBy;
}