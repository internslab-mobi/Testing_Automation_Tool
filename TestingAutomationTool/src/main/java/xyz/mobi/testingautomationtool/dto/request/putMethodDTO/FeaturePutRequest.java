package xyz.mobi.testingautomationtool.dto.request.putMethodDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.FeatureStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeaturePutRequest {

    private String featureName;

    private String description;

    private FeatureStatus status;

    private Integer sprint;

    private String version;
}
