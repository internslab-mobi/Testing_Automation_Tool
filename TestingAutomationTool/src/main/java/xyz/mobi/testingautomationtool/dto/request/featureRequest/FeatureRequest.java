package xyz.mobi.testingautomationtool.dto.request.featureRequest;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.FeatureStatus;

import java.time.LocalTime;

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

    @NotNull(message = "Feature status cannot be null")
    private FeatureStatus status;

    @NotNull(message = "Duration cannot be null")
    private LocalTime duration;

    @NotNull(message = "Sprint cannot be null")
    private Integer sprint;

    @NotBlank(message = "Version cannot be blank")
    private String version;

    @NotNull(message = "Created by cannot be null")
    private Integer createdBy;
}
