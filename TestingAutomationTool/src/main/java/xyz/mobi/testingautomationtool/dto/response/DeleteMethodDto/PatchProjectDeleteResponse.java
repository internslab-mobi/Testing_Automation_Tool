package xyz.mobi.testingautomationtool.dto.response.DeleteMethodDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchProjectDeleteResponse {
    private Integer projectId;
    private String message;
}
