package xyz.mobi.testingautomationtool.dto.request.postMethodDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
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
public class BugRequest {

    @NotBlank
    @Size(max = 300)
    private String title;

    private String description;

    private BugSeverity severity;

    private BugPriority priority;

    private BugCategory category;

    private BugStatus status;

    private Integer assignedTo;

    private String comments;

    private Map<String, Object> dynamicFields;

}
