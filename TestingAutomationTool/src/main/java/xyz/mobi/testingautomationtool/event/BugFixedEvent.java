package xyz.mobi.testingautomationtool.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.mobi.testingautomationtool.entity.Bug;

@Getter
@RequiredArgsConstructor
public class BugFixedEvent {
    private final Bug bug;
}
