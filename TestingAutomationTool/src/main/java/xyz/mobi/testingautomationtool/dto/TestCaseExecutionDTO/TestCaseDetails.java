package xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseDetails {

    private Integer testcaseId;
    private String testcaseFormatId;
    private Integer featureId;
    private String title;
    private TestType testType;
    private TestPriority testPriority;
    private TestCaseStatus testcaseStatus;
}