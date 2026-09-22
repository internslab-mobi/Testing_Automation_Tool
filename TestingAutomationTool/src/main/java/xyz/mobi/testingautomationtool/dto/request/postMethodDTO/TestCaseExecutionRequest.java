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


    private TestCaseRequest testCase;

    private ExecutionRequest executionRequest;
}
