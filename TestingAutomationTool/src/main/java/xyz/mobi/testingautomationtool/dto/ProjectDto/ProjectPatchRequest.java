package xyz.mobi.testingautomationtool.dto.ProjectDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectPatchRequest {
    private String projectName;
    private String description;
    private String region;
    private String comments;
}
