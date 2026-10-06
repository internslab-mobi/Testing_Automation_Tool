package xyz.mobi.testingautomationtool.dto.DashboardDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerRecentBugDto {
    private Integer bugId;
    private String bugFormatId;
    private String title;
    private BugSeverity severity;
    private BugPriority priority;
    private BugStatus status;
    private BugCategory category;
    private Integer featureId;
    private String featureName;
    private Integer projectId;
    private String projectName;
    private String reportedBy;
    private String assignedTo;
    private Instant createdAt;
}
