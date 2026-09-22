package xyz.mobi.testingautomationtool.dto.request.postMethodDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseRequest {

    @Positive(message = "Feature ID must be greater than 0")
    private Integer featureId;

    @Size(max = 225, message = "Test case format ID cannot exceed 20 characters")
    private String testcaseFormatId;

    @Size(max = 500, message = "Title cannot exceed 500 characters")
    private String title;

    private TestType testType;

    private TestPriority testPriority;

    private java.util.Map<String, Object> dynamicFields;
}