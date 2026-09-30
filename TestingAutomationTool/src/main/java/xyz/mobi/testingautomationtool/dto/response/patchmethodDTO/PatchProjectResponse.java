package xyz.mobi.testingautomationtool.dto.response.patchmethodDTO;

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
public class PatchProjectResponse {

    private Integer projectId;
    private String projectName;
    private String description;
    private ProjectStatus status;
    private String region;
    private String updatedBy;
    private Instant updatedAt;
    private String comments;
}
