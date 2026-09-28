package xyz.mobi.testingautomationtool.dto.request.patchmethodDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BugPatchRequest {

    private BugAssignRequest assignment;

    private BugStatusRequest status;
}
