package xyz.mobi.testingautomationtool.dto.FeatureDTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureDurationPatchRequest {
    @NotNull(message = "running flag is required")
    private Boolean running;
}
