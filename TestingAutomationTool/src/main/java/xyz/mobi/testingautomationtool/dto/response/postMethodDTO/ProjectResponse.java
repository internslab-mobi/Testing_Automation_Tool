package xyz.mobi.testingautomationtool.dto.response.postMethodDTO;

import lombok.Builder;
import lombok.Data;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;

@Data
@Builder
public class ProjectResponse {

    private Integer projectId;
    private String projectName;
    private String description;
    private ProjectStatus status;
    private String region;
    private Integer createdBy;
    private boolean isActive;
    private boolean isDeleted;
    private Long version;
}
