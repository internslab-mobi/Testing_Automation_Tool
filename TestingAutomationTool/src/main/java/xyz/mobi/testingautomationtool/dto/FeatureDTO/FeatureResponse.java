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
public class FeatureResponse {

    // --- Legacy Flat Fields (Commented to prevent duplicate keys in JSON response) ---
    // private Integer featureId;
    // private Integer projectId;
    // private String projectName;
    // private String featureName;
    // private String description;
    // private FeatureStatus status;
    // private Long duration;
    // private Integer sprint;
    // private String featureVersion;
    // private Instant startTime;
    // private boolean isActive;
    // private Integer createdBy;
    // private String creatorName;
    // private Instant createdAt;
    // private String comments;

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
        private Long duration;
        private Integer sprint;
        private String featureVersion;
        private Instant startTime;
        private String comments;
        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProjectRef {
        private Integer projectId;
        private String projectName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AuditResponse {
        private Integer createdBy;
        private String creatorName;
        private Integer updatedBy;
        private String updatedByName;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
