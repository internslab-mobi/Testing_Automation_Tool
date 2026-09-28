package xyz.mobi.testingautomationtool.service;

import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.NotificationRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.NotificationResponse;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(
            NotificationRequest request);

    void createReassignNotification(
            Integer oldAssignedId,
            Integer newAssignedId,
            Integer bugId
    );

    List<NotificationResponse> getByAll();

    List<NotificationResponse> getByAssignedId(
            Integer assignedId
    );

   void deleteNotification(Integer notificationId);
}