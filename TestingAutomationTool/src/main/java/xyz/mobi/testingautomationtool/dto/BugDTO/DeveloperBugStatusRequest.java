package xyz.mobi.testingautomationtool.dto.BugDto;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.DeveloperBugStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeveloperBugStatusRequest {

    private DeveloperBugStatus status;
}
