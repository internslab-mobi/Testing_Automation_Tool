package xyz.mobi.testingautomationtool.dto.BugDTO.GetBugResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseInfo {
    private Integer testcaseId;
    private String testcaseFormatId;
}
