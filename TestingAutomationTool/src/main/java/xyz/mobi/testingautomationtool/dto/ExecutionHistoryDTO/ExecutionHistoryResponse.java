package xyz.mobi.testingautomationtool.dto.ExecutionHistoryDTO;

import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionHistoryResponse {

    private Integer historyId;

    private Integer executionId;

    private TestCaseStatus status;

    private Integer changedBy;

    private LocalDateTime changedAt;

    private String comments;
}
