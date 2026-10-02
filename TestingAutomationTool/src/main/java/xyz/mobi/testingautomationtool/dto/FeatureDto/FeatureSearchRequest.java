package xyz.mobi.testingautomationtool.dto.FeatureDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.FeatureStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureSearchRequest {

    private String keyword;
    private Integer projectId;
    private String featureName;
    private String description;
    private FeatureStatus status;
    private Integer sprint;
    private String featureVersion;
    private String createdBy;
    private String updatedBy;
    private String comments;
}
