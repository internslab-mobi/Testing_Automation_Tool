package xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.dto.TestCaseDTO.TestCaseRequest;
import xyz.mobi.testingautomationtool.dto.TestingExecutionDTO.TestingExecutionRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseExecutionRequest {

    @NotNull(message = "Test case data is required")
    @Valid
    private TestCaseRequest testCase;

    @NotNull(message = "Testing execution data is required")
    @Valid
    private TestingExecutionRequest testingExecutionRequest;
}
