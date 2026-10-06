package xyz.mobi.testingautomationtool.dto.ProjectDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
