package xyz.mobi.testingautomationtool.dto.BugDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugPatchRequest {

    private BugAssignRequest assignment;

    private BugStatusRequest status;
}
