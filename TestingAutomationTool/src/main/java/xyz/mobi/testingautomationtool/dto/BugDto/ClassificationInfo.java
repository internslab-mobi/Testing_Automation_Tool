package xyz.mobi.testingautomationtool.dto.BugDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationInfo {
    private BugSeverity severity;
    private BugPriority priority;
    private BugCategory category;
    private BugStatus status;
}
