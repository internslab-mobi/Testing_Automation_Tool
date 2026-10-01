package xyz.mobi.testingautomationtool.dto.FeatureDto;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.FeatureStatus;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeaturePutResponse {

    private Integer featureId;
    private Integer projectId;
    private String featureName;
    private String description;
    private FeatureStatus status;
    private Integer sprint;
    private String version;
    private String featureVersion;
    private Long duration;
    private Instant startTime;
    private Integer createdBy;
}
