package xyz.mobi.testingautomationtool.dto.TestCaseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDto.PatchExecutionResponse;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchTestCaseResponse {
    private Integer testcaseId;
    private Integer featureId;
    private String testcaseFormatId;
    private TestType testType;
    private TestPriority testPriority;
    private TestCaseStatus testcaseStatus;
    private Boolean isActive;
    private String username;
    private Instant updatedAt;
    private Map<String, Object> dynamicFields;
    private PatchExecutionResponse execution;
}
