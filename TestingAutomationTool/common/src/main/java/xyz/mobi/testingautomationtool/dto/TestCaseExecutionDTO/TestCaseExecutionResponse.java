package xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.dto.TestCaseDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.dto.TestingExecutionDTO.TestingExecutionResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseExecutionResponse {
    private TestCaseResponse testCaseResponse;
    private TestingExecutionResponse testingExecutionResponse;
}