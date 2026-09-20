package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.NotificationResponse;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getMyNotifications(
            Integer employeeId);

    NotificationResponse getById(
            Integer notificationId);

    NotificationResponse createNotification(
            Integer employeeId,
            Integer bugId,
            String message);

    void updateStatus(
            Integer notificationId,
            NotificationStatus status);
}