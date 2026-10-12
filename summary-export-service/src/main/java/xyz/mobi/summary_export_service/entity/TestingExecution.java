package xyz.mobi.summary_export_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "testing_executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestingExecution {

    @Id
    @Column(name = "execution_id")
    private Integer executionId;

    @Column(name = "testcase_id", nullable = false)
    private Integer testcaseId;

    @Column(name = "bugs_count")
    private Integer bugsCount;

    @Column(name = "execution_number")
    private Integer executionNumber;

    @Column(name = "automation_feasibility")
    private String automationFeasibility;

    @Column(name = "execution_status")
    private String executionStatus;

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
    private Instant executedAt;
}
