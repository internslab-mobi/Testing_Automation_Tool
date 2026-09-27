package xyz.mobi.testingautomationtool.dto.response.patchmethodDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;
import xyz.mobi.testingautomationtool.enums.ExecutionStatus;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchExecutionResponse {

    private String comments;

    private ExecutionStatus executionStatus;

    private AutomationFeasibility automationFeasibility;
}
