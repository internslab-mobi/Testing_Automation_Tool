package xyz.mobi.testingautomationtool.dto.FeatureDto;

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
