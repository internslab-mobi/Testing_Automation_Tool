package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "testing_executions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_testcase_execution",
                        columnNames = {"testcase_id", "bugs_count"}
                )
        }
)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "testcase_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_execution_testcase")
    )
    private TestCase testCase;

    @Min(value = 1, message = "Bugs count must be greater than 0")
    @Column(name = "bugs_count", nullable = false)
    private Integer bugsCount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "automation_feasibility",
            nullable = false,
            length = 20
    )
    private AutomationFeasibility automationFeasibility;

    @Lob
    @Column(name = "test_execution")
    private String testExecution;

    @Lob
    @Column(name = "test_validation")
    private String testValidation;

    @Lob
    @Column(name = "precondition")
    private String precondition;

    @Lob
    @Column(name = "test_data")
    private String testData;

    @Lob
    @Column(name = "execution_steps")
    private String executionSteps;

    @Lob
    @Column(name = "ui_validations")
    private String uiValidations;

    @Lob
    @Column(name = "db_validations")
    private String dbValidations;

    @Lob
    @Column(name = "comments")
    private String comments;

    @Column(name = "executed_by", nullable = false)
    private Integer executedBy;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;
}
