package xyz.mobi.testingautomationtool.dto.response.putMethodDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCasePutResponse {

    private Integer testcaseId;

    private Integer featureId;

    private String title;

    private TestType testType;

    private TestPriority testPriority;

    private TestCaseStatus testcaseStatus;

    private String username;

    private LocalDateTime updatedAt;

    private String testcaseFormatId;

    private String testExecution;

    private String testValidation;

    private String precondition;

    private String testData;

    private String executionSteps;

    private String uiValidations;

    private String dbValidations;

    private String comments;

    private java.util.Map<String, Object> dynamicFields;
}
