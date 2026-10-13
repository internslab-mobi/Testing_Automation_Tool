package xyz.mobi.testingautomationtool.dto.DashboardDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatchRequestOfManager {

    private String role;

    private Integer userId;
}
