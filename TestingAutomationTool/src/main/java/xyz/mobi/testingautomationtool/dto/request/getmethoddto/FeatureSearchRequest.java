package xyz.mobi.testingautomationtool.dto.request.getmethoddto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeatureSearchRequest {

    private String keyword;

    private Integer projectId;

    private String featureName;

    private String status;

    private String sprint;

    private String version;
}
