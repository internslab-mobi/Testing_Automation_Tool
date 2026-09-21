package xyz.mobi.testingautomationtool.dto.response.postMethodDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseExecutionResponse {

    private TestCaseResponse testCaseResponse;

    private ExecutionResponse executionResponse;

}
