package xyz.mobi.testingautomationtool.dto.FeatureDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
