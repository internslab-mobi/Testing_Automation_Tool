package xyz.mobi.testingautomationtool.dto.FeatureDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.FeatureStatus;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeaturePatchRequest {
    private String featureName;
    private String description;
    private FeatureStatus status;
    private Long duration;
    private Integer sprint;
    private String version;
    private Instant startTime;
    private Boolean isActive;
    private Integer updatedBy;

    private Long lockVersion;
}
