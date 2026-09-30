package xyz.mobi.testingautomationtool.dto.request.patchmethodDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectPatchRequest {

    private String projectName;

    private String description;

    private String region;

    private String comments;
}
