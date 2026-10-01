package xyz.mobi.testingautomationtool.dto.TestCaseExecutionDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.dto.TestCaseDto.TestCaseResponse;
import xyz.mobi.testingautomationtool.dto.TestingExecutionDto.TestingExecutionResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseExecutionResponse {
    private TestCaseResponse testCaseResponse;
    private TestingExecutionResponse testingExecutionResponse;
}