package xyz.mobi.testingautomationtool.dto.request.managerRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.Role;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatchRequestOfManager {

    private Role role;

    private Integer userId;

}



