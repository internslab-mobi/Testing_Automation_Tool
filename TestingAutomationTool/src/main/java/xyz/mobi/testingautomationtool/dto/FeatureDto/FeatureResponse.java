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
public class FeatureResponse {

    private Integer featureId;
    private Integer projectId;
    private String projectName;
    private String featureName;
    private String description;
    private FeatureStatus status;
    private Long duration;
    private Integer sprint;
    private String featureVersion;
    private Instant startTime;
    private boolean isActive;
    private Integer createdBy;
    private String creatorName;
    private Instant createdAt;
    private String comments;
}
