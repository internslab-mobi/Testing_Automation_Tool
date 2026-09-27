package xyz.mobi.testingautomationtool.dto.response.patchmethodDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.*;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
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
