package xyz.mobi.testingautomationtool.dto.response.featureResponse;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.FeatureStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureResponse {

    private Integer featureId;

    private Integer projectId;

    private String featureName;

    private String description;

    private FeatureStatus status;

    private LocalTime duration;

    private Integer sprint;

    private String version;

    private String createdBy;

    private String testcaseFileName;

    private String testcaseFileType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
