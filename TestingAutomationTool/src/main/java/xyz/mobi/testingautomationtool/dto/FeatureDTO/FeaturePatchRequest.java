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
    private String featureVersion;
    private Instant startTime;
    private Boolean startTimer;
    private Boolean endTimer;
    private Integer days;
    private Integer hours;
    private Integer minutes;
    private String comments;
    private Boolean isActive;
    private Integer updatedBy;
}
