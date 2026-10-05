package xyz.mobi.testingautomationtool.dto.BugDto.GetBugResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private String reportedBy;
    private String assignedTo;
    private String executedBy;
    private String updatedBy;
}
