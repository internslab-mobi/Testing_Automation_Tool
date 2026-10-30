package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.NotificationDTO.InAppNotificationResponse;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.Comment;
import xyz.mobi.testingautomationtool.entity.Project;

import java.util.List;

public interface InAppNotificationService {
    void createBugFixedNotification(Bug bug);

    List<InAppNotificationResponse> getMyNotifications();

    void markAsRead(Integer notificationId);

    String patchNotification(Integer notificationId, xyz.mobi.testingautomationtool.dto.NotificationDTO.NotificationPatchRequest request);

    void deleteNotification(Integer notificationId);

    void createNewCommentNotification(Comment comment);

    void createBugAssignedNotification(Bug bug);

    void createProjectInactiveNotification(Project project);
}
