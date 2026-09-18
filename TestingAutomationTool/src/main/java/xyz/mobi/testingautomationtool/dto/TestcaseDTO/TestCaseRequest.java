package xyz.mobi.testingautomationtool.dto.TestcaseDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseRequest {

    @NotNull(message = "Feature ID is required")
    @Positive(message = "Feature ID must be greater than 0")
    private Integer featureId;

    @NotBlank(message = "Title is required")
    @Size(max = 300, message = "Title cannot exceed 300 characters")
    private String title;

    @NotNull(message = "Test type is required")
    private TestType testType;

    @NotNull(message = "Test priority is required")
    private TestPriority testPriority;

//    @NotNull(message = "Test case status is required")
//    private TestCaseStatus testcaseStatus;

    @NotNull(message = "Created by is required")
    @Positive(message = "Created by must be greater than 0")
    private Integer createdBy;

    @NotBlank(message = "Test case format ID is required")
    @Size(max = 20, message = "Test case format ID cannot exceed 20 characters")
    private String testcaseFormatId;
}