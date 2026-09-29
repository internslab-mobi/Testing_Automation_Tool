package xyz.mobi.testingautomationtool.dto.CommentDTO;

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
    private Integer createdBy;
    private String creatorName;
    private Instant createdAt;
    //private Instant updatedAt;
}
