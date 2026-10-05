package xyz.mobi.testingautomationtool.dto.BugDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;
import xyz.mobi.testingautomationtool.enums.DeveloperBugStatus;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugPatchRequest {

//    private String title;
//    private String description;
//    private BugSeverity severity;
//    private BugPriority priority;
//    private BugCategory category;
//    private BugStatus status;
//    private DeveloperBugStatus developerStatus;
//    private Integer assignedTo;
//    private String comments;
//    private Integer bugOccurrence;
//    private Boolean isActive;
//    private Map<String, Object> dynamicFields;
//
//    // Optional legacy nested request support
//    private BugAssignRequest assignment;
//    private BugStatusRequest statusRequest;
//
//    public Integer getEffectiveAssignedTo() {
//        if (assignedTo != null) return assignedTo;
//        if (assignment != null) return assignment.getAssignedTo();
//        return null;
//    }
//
//    public BugStatus getEffectiveStatus() {
//        if (status != null) return status;
//        if (statusRequest != null) return statusRequest.getStatus();
//        return null;
//    }

    private String title;

    private String description;

    private BugPriority priority;

    private BugSeverity severity;

    private BugStatus status;

    private BugCategory category;

    private Integer assignedTo;

    private String comments;

    private Map<String, Object> dynamicFields;
}
