package xyz.mobi.testingautomationtool.dto.BugDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperOptionResponse {
    private Integer userId;
    private String fullName;
    private String username;
}
