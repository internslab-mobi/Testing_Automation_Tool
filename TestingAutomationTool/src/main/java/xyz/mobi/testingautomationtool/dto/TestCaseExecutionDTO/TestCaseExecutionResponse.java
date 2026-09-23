package xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.dto.TestcaseDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.dto.TestingExecutionDTO.TestingExecutionResponse;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseExecutionResponse {

    private TestCaseResponse testCaseResponse;
    private TestingExecutionResponse testingExecutionResponse;
}