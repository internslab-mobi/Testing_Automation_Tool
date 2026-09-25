package xyz.mobi.testingautomationtool.dto.request.patchmethodDTO;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugAssignRequest {

    @NotNull(message = "Assigned user is required")
    private Integer assignedTo;
}