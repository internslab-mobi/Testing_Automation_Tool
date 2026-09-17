package xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.dto.TestcaseDTO.TestCaseRequest;
import xyz.mobi.testingautomationtool.dto.TestingExecutionDTO.TestingExecutionRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseExecutionRequest {

    private TestCaseRequest testCase;

    private TestingExecutionRequest testingExecutionRequest;
}
