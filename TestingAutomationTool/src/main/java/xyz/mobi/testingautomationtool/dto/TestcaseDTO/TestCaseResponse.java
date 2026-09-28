package xyz.mobi.testingautomationtool.dto.TestcaseDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseResponse {

    private TestCaseDetails testCase;

    private ExecutionResponse execution;

    private ValidationResponse validations;

    private AutomationResponse automation;

    private BugSummaryResponse bugSummary;

    private String comments;

    private AuditResponse audit;
}