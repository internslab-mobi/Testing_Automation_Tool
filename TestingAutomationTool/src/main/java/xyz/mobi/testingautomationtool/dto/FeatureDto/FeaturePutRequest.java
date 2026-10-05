package xyz.mobi.testingautomationtool.dto.FeatureDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.FeatureStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeaturePutRequest {

    private String featureName;

    private String description;

    private Integer sprint;

    private String featureVersion;
}
