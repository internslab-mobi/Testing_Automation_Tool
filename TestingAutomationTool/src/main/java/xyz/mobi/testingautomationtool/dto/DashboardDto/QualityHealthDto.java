package xyz.mobi.testingautomationtool.dto.DashboardDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityHealthDto {
    private Double score;
    private String status; // "EXCELLENT", "GOOD", "NEEDS_ATTENTION", "CRITICAL"
    private String summary;
    private Double passRate;
    private Long criticalDefectCount;
    private Long highDefectCount;
    private Long totalExecuted;
}
