package xyz.mobi.testingautomationtool.dto.TestCaseDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseRequest {

    @NotNull(message = "Feature ID is required")
    @Positive(message = "Feature ID must be greater than 0")
    private Integer featureId;

    @Size(max = 225, message = "Test case format ID cannot exceed 225 characters")
    private String testcaseFormatId;

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title cannot exceed 500 characters")
    private String title;

    private TestType testType;

    private TestPriority testPriority;

    private TestCaseStatus testcaseStatus;

    private Integer createdBy;

    private Map<String, Object> dynamicFields;
}