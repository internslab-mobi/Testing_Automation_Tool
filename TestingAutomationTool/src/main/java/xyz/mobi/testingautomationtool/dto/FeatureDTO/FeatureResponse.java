package xyz.mobi.testingautomationtool.dto.FeatureDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.FeatureStatus;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureResponse {
    private Integer featureId;
    private Integer projectId;
    private String projectName;
    private String featureName;
    private String description;
    private FeatureStatus status;
    private Long duration;
    private Integer sprint;
    private String version;
    private Instant startTime;
    private boolean isActive;
    private Integer createdBy;
    private Integer updatedBy;
    private Instant createdAt;
    private Instant updatedAt;

    private Long lockVersion;        // optimistic locking version
}
