package xyz.mobi.testingautomationtool.dto.TestingExecutionDTO;

import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestingExecutionResponse {

    private Integer executionId;

    private Integer testcaseId;

    private Integer bugsCount;

    private AutomationFeasibility automationFeasibility;

    private String testExecution;

    private String testValidation;

    private String precondition;

    private String testData;

    private String executionSteps;

    private String uiValidations;

    private String dbValidations;

    private String comments;

    private Integer executedBy;

    private LocalDateTime executedAt;
}
