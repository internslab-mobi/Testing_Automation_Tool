package xyz.mobi.testingautomationtool.dto.BugDto.GetBugResponse;

import lombok.*;
import xyz.mobi.testingautomationtool.dto.BugDto.AuditInfo;
import xyz.mobi.testingautomationtool.dto.BugDto.ClassificationInfo;
import xyz.mobi.testingautomationtool.dto.BugDto.ResolutionInfo;
import xyz.mobi.testingautomationtool.dto.BugDto.UserInfo;

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