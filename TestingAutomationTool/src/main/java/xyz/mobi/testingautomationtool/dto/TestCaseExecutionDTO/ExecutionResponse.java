package xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionResponse {
    private Integer executionId;
    private String testExecution;
    private String precondition;
    private String testData;
    private String executionSteps;
}