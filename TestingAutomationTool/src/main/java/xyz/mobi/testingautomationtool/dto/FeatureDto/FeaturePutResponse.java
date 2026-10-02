package xyz.mobi.testingautomationtool.dto.FeatureDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.FeatureStatus;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeaturePutResponse {

    private Integer featureId;
    private Integer projectId;
    private String featureName;
    private String description;
    private FeatureStatus status;
    private Integer sprint;
    private String featureVersion;
    private Long duration;
    private Instant startTime;
    private Integer updatedBy;
}
