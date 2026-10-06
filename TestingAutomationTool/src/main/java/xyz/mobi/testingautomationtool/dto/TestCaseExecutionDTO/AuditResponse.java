package xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditResponse {
    private Instant createdAt;
    private Instant updatedAt;
}