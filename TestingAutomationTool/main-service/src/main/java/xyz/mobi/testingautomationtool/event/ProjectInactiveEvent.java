package xyz.mobi.testingautomationtool.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.mobi.testingautomationtool.entity.Project;

@Getter
@RequiredArgsConstructor
public class ProjectInactiveEvent {
    private final Project project;
}
