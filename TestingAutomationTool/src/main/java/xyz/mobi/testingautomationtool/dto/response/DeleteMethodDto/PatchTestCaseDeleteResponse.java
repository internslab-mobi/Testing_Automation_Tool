package xyz.mobi.testingautomationtool.dto.response.DeleteMethodDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchTestCaseDeleteResponse {
    private Integer testcaseId;
    private String message;

}
