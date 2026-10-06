package xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugSummaryResponse {
    private Integer bugsCount;
}