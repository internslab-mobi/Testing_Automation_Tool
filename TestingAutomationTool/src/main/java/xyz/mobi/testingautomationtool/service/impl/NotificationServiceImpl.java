package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.NotificationResponse;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.Notification;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;
import xyz.mobi.testingautomationtool.mapper.getMapper.NotificationMapper;
import xyz.mobi.testingautomationtool.repository.BugRepository;
import xyz.mobi.testingautomationtool.repository.NotificationRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final BugRepository bugRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public List<NotificationResponse> getMyNotifications(
            Integer employeeId) {

        return notificationRepository
                .findByEmployee_UserIdOrderByCreatedAtDesc(employeeId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public NotificationResponse getById(
            Integer notificationId) {

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found with ID: "
                                                + notificationId));

        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(
            Integer employeeId,
            Integer bugId,
            String message) {

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with ID: "
                                        + employeeId));

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bug not found with ID: "
                                        + bugId));

        Notification notification = Notification.builder()
                .employee(employee)
                .bug(bug)
                .message(message)
                .createdAt(LocalDateTime.now())
                .notificationStatus(NotificationStatus.PENDING)
                .build();

        notification =
                notificationRepository.save(notification);

        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public void updateStatus(
            Integer notificationId,
            NotificationStatus status) {

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found with ID: "
                                                + notificationId));

        notification.setNotificationStatus(status);

        notificationRepository.save(notification);
    }
}