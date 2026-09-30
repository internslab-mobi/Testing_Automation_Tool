package xyz.mobi.testingautomationtool.dto.TestcaseDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private Integer featureId;

    @NotBlank
    @Size(max = 300)
    private String title;

    @NotNull
    private TestType testType;

    private TestPriority testPriority;

    private TestCaseStatus testcaseStatus;

    @NotNull
    private Integer createdBy;

    @NotBlank
    @Size(max = 20)
    private String testcaseFormatId;
}