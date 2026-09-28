package xyz.mobi.testingautomationtool.dto.response.postMethodDTO;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {

    private Integer commentId;
    private Integer bugId;
    private String comment;
    private Integer createdByUserId;
    private String createdByUsername;
    private String createdByFullName;
    private Instant createdAt;
}
