package xyz.mobi.testingautomationtool.dto.FeatureDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureStartTimeResponse {
    private Instant startTime;
}
