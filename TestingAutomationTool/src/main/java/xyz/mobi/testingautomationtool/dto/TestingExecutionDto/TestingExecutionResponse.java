package xyz.mobi.testingautomationtool.dto.TestingExecutionDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
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
    private Instant executedAt;
}
