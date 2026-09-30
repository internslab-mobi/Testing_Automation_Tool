package xyz.mobi.testingautomationtool.dto.ExecutionHistoryDTO;


import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionHistoryRequest {

    @NotNull
    private Integer executionId;

    @NotNull
    private TestCaseStatus status;

    @NotNull
    private Integer changedBy;

    private String comments;
}
