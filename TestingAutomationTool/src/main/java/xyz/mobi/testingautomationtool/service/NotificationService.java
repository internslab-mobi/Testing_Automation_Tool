package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.NotificationDto.NotificationResponse;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.Notification;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;

import java.util.List;

public interface NotificationService {

    Notification createNotification(User reporter, User assignedTo, Bug bug);

    void updateNotificationStatus(Integer notificationId, NotificationStatus status);

    List<NotificationResponse> getMyNotifications(Integer employeeId);

    NotificationResponse getById(Integer notificationId);

    void updateStatus(Integer notificationId, NotificationStatus status);
}