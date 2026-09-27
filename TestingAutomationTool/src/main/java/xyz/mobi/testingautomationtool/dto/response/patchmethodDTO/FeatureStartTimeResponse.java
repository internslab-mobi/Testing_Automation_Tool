package xyz.mobi.testingautomationtool.dto.response.patchmethodDTO;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureStartTimeResponse {

    private Instant startTime;
}
