package xyz.mobi.testingautomationtool.dto.request.patchmethodDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;
import xyz.mobi.testingautomationtool.enums.ExecutionStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestCasePatchRequest {

    private TestType testType;

    private TestPriority testPriority;

    private  Boolean isActive;

    private String comments;

    private java.util.Map<String, Object> dynamicFields;

    private ExecutionStatus executionStatus;

    private AutomationFeasibility automationFeasibility;

}
