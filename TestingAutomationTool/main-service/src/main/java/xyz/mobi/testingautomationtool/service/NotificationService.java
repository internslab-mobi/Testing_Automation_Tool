package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.NotificationDTO.NotificationRequest;
import xyz.mobi.testingautomationtool.dto.NotificationDTO.NotificationResponse;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(NotificationRequest request);

    void createReassignNotification(Integer oldAssignedId, Integer newAssignedId, Integer bugId);

    void updateNotificationStatus(Integer notificationId, NotificationStatus status);

    List<NotificationResponse> getMyNotifications(Integer employeeId);

    NotificationResponse getById(Integer notificationId);

    List<NotificationResponse> getByAll();

    void deleteNotification(Integer notificationId);
}
