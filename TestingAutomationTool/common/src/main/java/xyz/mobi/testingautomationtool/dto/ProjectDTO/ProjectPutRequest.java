package xyz.mobi.testingautomationtool.dto.ProjectDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectPutRequest {
    @NotBlank(message = "Project name is required")
    @Size(max = 200, message = "Project name cannot exceed 200 characters")
    private String projectName;

    private String description;

    @NotNull(message = "Region is required")
    private Integer region;

    @NotNull(message = "Module is required")
    private Integer module;

    @NotNull(message = "SubModule is required")
    private Integer subModule;

    private String comments;
}
