package xyz.mobi.testingautomationtool.dto.response.postMethodDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseResponse {


    private Integer featureId;

    private String testcaseFormatId;

    private String title;

    private TestType testType;

    private TestPriority testPriority;

    private TestCaseStatus testcaseStatus;

    private String createdBy;

    private Instant createdAt;

    private java.util.Map<String, Object> dynamicFields;
}
