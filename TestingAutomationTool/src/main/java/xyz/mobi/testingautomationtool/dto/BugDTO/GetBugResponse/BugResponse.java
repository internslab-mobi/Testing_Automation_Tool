package xyz.mobi.testingautomationtool.dto.BugDTO.GetBugResponse;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BugResponse {

    private Integer bugId;
    private String bugFormatId;
    private String title;
    private String description;

    private TestCaseInfo testCase;

    private FeatureInfo feature;

    private ClassificationInfo classification;

    private UserInfo users;

    private ResolutionInfo resolution;

    private String comments;

    private AuditInfo audit;

    private boolean active;
}