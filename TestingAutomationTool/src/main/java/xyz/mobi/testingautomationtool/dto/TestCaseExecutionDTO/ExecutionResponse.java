package xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO;

import lombok.*;

@Getter
@Setter
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