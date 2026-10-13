package xyz.mobi.testingautomationtool.dto.ProjectDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchProjectDeleteResponse {
    private Integer projectId;
    private String message;
}
