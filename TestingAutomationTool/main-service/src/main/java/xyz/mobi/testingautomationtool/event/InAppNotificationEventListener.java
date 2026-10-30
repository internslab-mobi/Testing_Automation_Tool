package xyz.mobi.testingautomationtool.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import xyz.mobi.testingautomationtool.service.InAppNotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class InAppNotificationEventListener {

    private final InAppNotificationService inAppNotificationService;

    @EventListener
    public void handleBugFixedEvent(BugFixedEvent event) {
        if (event == null || event.getBug() == null) {
            return;
        }
        log.info("Handling BugFixedEvent for Bug ID: {}", event.getBug().getBugId());
        try {
            inAppNotificationService.createBugFixedNotification(event.getBug());
        } catch (Exception ex) {
            log.error("Failed to create bug fixed in-app notification for Bug ID {}: {}",
                    event.getBug().getBugId(), ex.getMessage(), ex);
        }
    }

    @EventListener
    public void handleCommentAddedEvent(CommentAddedEvent event) {
        if (event == null || event.getComment() == null) {
            return;
        }
        log.info("Handling CommentAddedEvent for Comment ID: {}", event.getComment().getCommentId());
        try {
            inAppNotificationService.createNewCommentNotification(event.getComment());
        } catch (Exception ex) {
            log.error("Failed to create new comment in-app notification for Comment ID {}: {}",
                    event.getComment().getCommentId(), ex.getMessage(), ex);
        }
    }

    @EventListener
    public void handleBugAssignedEvent(BugAssignedEvent event) {
        if (event == null || event.getBug() == null) {
            return;
        }
        log.info("Handling BugAssignedEvent for Bug ID: {}", event.getBug().getBugId());
        try {
            inAppNotificationService.createBugAssignedNotification(event.getBug());
        } catch (Exception ex) {
            log.error("Failed to create bug assigned in-app notification for Bug ID {}: {}",
                    event.getBug().getBugId(), ex.getMessage(), ex);
        }
    }

    @EventListener
    public void handleProjectInactiveEvent(ProjectInactiveEvent event) {
        if (event == null || event.getProject() == null) {
            return;
        }
        log.info("Handling ProjectInactiveEvent for Project ID: {}", event.getProject().getProjectId());
        try {
            inAppNotificationService.createProjectInactiveNotification(event.getProject());
        } catch (Exception ex) {
            log.error("Failed to create project inactive in-app notification for Project ID {}: {}",
                    event.getProject().getProjectId(), ex.getMessage(), ex);
        }
    }
}
