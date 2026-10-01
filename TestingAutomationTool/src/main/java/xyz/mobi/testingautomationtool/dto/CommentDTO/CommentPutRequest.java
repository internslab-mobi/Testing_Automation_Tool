package xyz.mobi.testingautomationtool.dto.CommentDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentPutRequest {

    @NotBlank(message = "Comment cannot be blank")
    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String comment;
}
