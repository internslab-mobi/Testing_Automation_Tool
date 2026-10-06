package xyz.mobi.testingautomationtool.dto.ProjectDTO;

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
public class ProjectResponse {
    private Integer projectId;
    private String projectName;
    private String description;
    private ProjectStatus status;
    private String region;
    private String comments;
    private Integer createdBy;
    private String createdByName;
    private String updatedByName;
    private Boolean isActive;
    private Boolean isDeleted;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}
