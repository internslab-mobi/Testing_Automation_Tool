package xyz.mobi.testingautomationtool.dto.FeatureDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureSearchRequest {
    private String keyword;
    private Integer projectId;
    private String featureName;
    private String status;
    private Integer sprint;
    private String version;
}
