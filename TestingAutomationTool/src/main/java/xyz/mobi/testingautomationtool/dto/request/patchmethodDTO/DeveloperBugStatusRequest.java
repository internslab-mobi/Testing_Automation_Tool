package xyz.mobi.testingautomationtool.dto.request.patchmethodDTO;


import lombok.Getter;
import lombok.Setter;
import xyz.mobi.testingautomationtool.enums.DeveloperBugStatus;

@Getter
@Setter
public class DeveloperBugStatusRequest {

    private DeveloperBugStatus status;
}
