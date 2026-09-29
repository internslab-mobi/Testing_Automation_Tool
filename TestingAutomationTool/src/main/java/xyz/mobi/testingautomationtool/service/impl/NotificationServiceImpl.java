package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.Notification;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.repository.NotificationRepository;
import xyz.mobi.testingautomationtool.service.NotificationService;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public Notification createNotification(User reporter, User assignedTo, Bug bug) {
        Notification notification = Notification.builder()
                .employee(reporter)
                .assigned(assignedTo)
                .bug(bug)
                .createdAt(Instant.now())
                .notificationStatus(NotificationStatus.PENDING)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created with ID {} for bug {}", saved.getNotificationId(), bug.getBugFormatId());
        return saved;
    }

    @Override
    @Transactional
    public void updateNotificationStatus(Integer notificationId, NotificationStatus status) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        notification.setNotificationStatus(status);
        notificationRepository.save(notification);
        log.info("Updated notification {} status to {}", notificationId, status);
    }
}
