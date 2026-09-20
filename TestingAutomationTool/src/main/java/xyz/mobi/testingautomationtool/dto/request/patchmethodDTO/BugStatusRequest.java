package xyz.mobi.testingautomationtool.dto.request.patchmethodDTO;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.BugStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugStatusRequest {

    @NotNull(message = "Bug status is required")
    private BugStatus status;
}