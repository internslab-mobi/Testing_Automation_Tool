package xyz.mobi.testingautomationtool.dto.response.patchmethodDTO;


import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;
import xyz.mobi.testingautomationtool.enums.ExecutionStatus;

public record ExecutionStatusResponse(
        Integer testCaseId,
        String executedBy,
        ExecutionStatus executionStatus,
        AutomationFeasibility automationFeasibility) {
}