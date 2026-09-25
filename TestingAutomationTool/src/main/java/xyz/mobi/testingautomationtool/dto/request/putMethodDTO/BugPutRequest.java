package xyz.mobi.testingautomationtool.dto.request.putMethodDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugPutRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private BugPriority priority;

    private BugSeverity severity;

    private BugStatus status;

    private BugCategory category;

    private User executed_by;

    private User assignedTo;

    private String comments;

    private Map<String, Object> dynamicFields;
}
