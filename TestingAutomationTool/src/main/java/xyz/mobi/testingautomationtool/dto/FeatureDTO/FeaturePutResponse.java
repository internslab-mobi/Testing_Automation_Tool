package xyz.mobi.testingautomationtool.dto.FeatureDTO;

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

    // --- Legacy Flat Fields (Commented to prevent duplicate keys in JSON response) ---
    // private Integer featureId;
    // private Integer projectId;
    // private String featureName;
    // private String description;
    // private FeatureStatus status;
    // private Integer sprint;
    // private String featureVersion;
    // private Long duration;
    // private Instant startTime;
    // private Integer updatedBy;

    // Structured inner JSON fields (using static inner classes)
    private FeatureDetails feature;
    private ProjectRef project;
    private AuditResponse audit;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FeatureDetails {
        private Integer featureId;
        private String featureName;
        private String description;
        private FeatureStatus status;
        private Integer sprint;
        private String featureVersion;
        private Long duration;
        private Instant startTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProjectRef {
        private Integer projectId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AuditResponse {
        private Integer updatedBy;
    }
}
