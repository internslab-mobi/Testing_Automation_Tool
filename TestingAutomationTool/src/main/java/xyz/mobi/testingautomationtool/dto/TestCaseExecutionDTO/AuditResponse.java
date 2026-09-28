package xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditResponse {

    private Instant createdAt;
    private Instant updatedAt;
}