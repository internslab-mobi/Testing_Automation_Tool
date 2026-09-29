package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.Notification;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;

public interface NotificationService {
    Notification createNotification(User reporter, User assignedTo, Bug bug);
    void updateNotificationStatus(Integer notificationId, NotificationStatus status);
}
