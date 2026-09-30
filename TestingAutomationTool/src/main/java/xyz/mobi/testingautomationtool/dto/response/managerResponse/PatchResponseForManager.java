package xyz.mobi.testingautomationtool.dto.response.managerResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatchResponseForManager {

    private String username;

    String message;

}
