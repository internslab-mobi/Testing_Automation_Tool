package xyz.mobi.testingautomationtool.dto.TestCaseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCasePutResponse {

    private Integer testcaseId;
    private Integer featureId;
    private String title;
    private TestType testType;
    private TestPriority testPriority;
    private String username;
    private Instant updatedAt;
    private TestCaseValidationResponse validation;
    private Map<String, Object> dynamicFields;
}
