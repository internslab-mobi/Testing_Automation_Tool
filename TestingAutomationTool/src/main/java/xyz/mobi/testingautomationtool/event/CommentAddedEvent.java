package xyz.mobi.testingautomationtool.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.mobi.testingautomationtool.entity.Comment;

@Getter
@RequiredArgsConstructor
public class CommentAddedEvent {
    private final Comment comment;
}
