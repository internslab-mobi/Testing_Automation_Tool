package xyz.mobi.testingautomationtool.dto.response.putMethodDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
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
