package xyz.mobi.testingautomationtool.dto.ProjectDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectPutRequest {
    @NotBlank(message = "Project name is required")
    @Size(max = 200, message = "Project name cannot exceed 200 characters")
    private String projectName;

    private String description;

    @NotBlank(message = "Region is required")
    @Size(max = 255, message = "Region cannot exceed 255 characters")
    private String region;


    private String comments;
}
