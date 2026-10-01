package xyz.mobi.testingautomationtool.dto.TestCaseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchTestCaseDeleteResponse {
    private Integer testcaseId;
    private String message;
}
