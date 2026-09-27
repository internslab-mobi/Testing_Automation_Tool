package xyz.mobi.testingautomationtool.dto.response.putMethodDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

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

    private Instant updatedAt;

    private String testcaseFormatId;

    private TestCaseValidationResponse validation;

    private Map<String, Object> dynamicFields;
}
