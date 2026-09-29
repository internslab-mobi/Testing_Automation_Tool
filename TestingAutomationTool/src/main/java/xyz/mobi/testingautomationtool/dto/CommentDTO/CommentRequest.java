package xyz.mobi.testingautomationtool.dto.CommentDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequest {

    @NotBlank(message = "Comment text is required")
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;

    @NotNull(message = "createdBy user ID is required")
    private Integer createdBy;
}
