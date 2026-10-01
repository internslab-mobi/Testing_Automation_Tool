package xyz.mobi.testingautomationtool.dto.TestCaseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;
import xyz.mobi.testingautomationtool.enums.ExecutionStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCasePatchRequest {
    private String title;
    private TestType testType;
    private TestPriority testPriority;
    private Boolean isActive;
    private String comments;
    private Map<String, Object> dynamicFields;
    private ExecutionStatus executionStatus;
    private AutomationFeasibility automationFeasibility;
}
