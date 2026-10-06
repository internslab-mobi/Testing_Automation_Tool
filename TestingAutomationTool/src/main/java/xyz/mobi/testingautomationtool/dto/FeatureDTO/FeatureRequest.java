package xyz.mobi.testingautomationtool.dto.FeatureDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureRequest {

    @NotNull(message = "Project ID cannot be null")
    private Integer projectId;

    @NotBlank(message = "Feature name cannot be blank")
    @Size(max = 200, message = "Feature name cannot exceed 200 characters")
    private String featureName;

    private String description;

    private String comments;

    @NotNull(message = "Sprint cannot be null")
    private Integer sprint;

    @NotBlank(message = "Version cannot be blank")
    private String featureVersion;

}
