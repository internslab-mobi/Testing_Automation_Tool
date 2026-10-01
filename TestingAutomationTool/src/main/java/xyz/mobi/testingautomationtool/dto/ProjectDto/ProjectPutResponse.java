package xyz.mobi.testingautomationtool.dto.ProjectDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectPutResponse {
    private Integer projectId;
    private String projectName;
    private String description;
    private ProjectStatus status;
    private String region;
    private boolean isActive;
    private boolean isDeleted;
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
}
