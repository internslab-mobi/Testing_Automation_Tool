package xyz.mobi.testingautomationtool.dto.response.getMethodDTO;


import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class FeatureResponse {

    private Integer featureId;

    private Integer projectId;

    private String featureName;

    private Long duration;

    private String sprint;

    private String version;

    private Integer createdBy;

    private String status;

    private boolean active;

    private Instant createdAt;

    private Instant updatedAt;
}
