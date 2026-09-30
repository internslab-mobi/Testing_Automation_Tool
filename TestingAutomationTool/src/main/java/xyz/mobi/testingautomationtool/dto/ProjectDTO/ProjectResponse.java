package xyz.mobi.testingautomationtool.dto.ProjectDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {
    private Integer projectId;
    private String projectName;
    private String description;
    private ProjectStatus status;
    private String region;
    private boolean isActive;
    private Integer createdBy;
    private String creatorName;
    private Integer updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
}
