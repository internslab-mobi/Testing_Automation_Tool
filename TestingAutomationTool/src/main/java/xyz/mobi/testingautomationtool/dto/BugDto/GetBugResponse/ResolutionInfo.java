package xyz.mobi.testingautomationtool.dto.BugDto.GetBugResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolutionInfo {
    private Instant resolvedAt;
    private Integer bugOccurrence;
}
