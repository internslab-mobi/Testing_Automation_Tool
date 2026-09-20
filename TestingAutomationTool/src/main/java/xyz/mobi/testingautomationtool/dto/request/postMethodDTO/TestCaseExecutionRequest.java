package xyz.mobi.testingautomationtool.dto.request.postMethodDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseExecutionRequest {

    @NotNull(message = "Test case data is required")
    @Valid
    private TestCaseRequest testCase;

    @NotNull(message = "Testing execution data is required")
    @Valid
    private ExecutionRequest executionRequest;
}
