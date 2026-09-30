package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.response.getMethodDTO.InAppNotificationResponse;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.Comment;
import xyz.mobi.testingautomationtool.entity.InAppNotification;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.InAppNotificationStatus;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.repository.InAppNotificationRepository;
import xyz.mobi.testingautomationtool.service.InAppNotificationService;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InAppNotificationServiceImpl implements InAppNotificationService {

    private final InAppNotificationRepository inAppNotificationRepository;

    @Transactional
    public void createBugFixedNotification(Bug bug) {

        InAppNotification notification = InAppNotification.builder()
                .employee(bug.getReportedBy())
                .bug(bug)
                .message("Bug " + bug.getBugFormatId() + " has been fixed")
                .notificationStatus(InAppNotificationStatus.UNREAD)
                .createdAt(Instant.now())
                .build();

        inAppNotificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InAppNotificationResponse> getMyNotifications() {

        Integer currentUserId = 1;

        return inAppNotificationRepository
                .findByEmployeeUserIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(notification ->
                        InAppNotificationResponse.builder()
                                .notificationId(notification.getNotificationId())
                                .bugId(notification.getBugId())
                                .bugFormatId(notification.getBugFormatId())
                                .message(notification.getMessage())
                                .notificationStatus(
                                        notification.getNotificationStatus())
                                .createdAt(notification.getCreatedAt())
                                .build()
                )
                .toList();
    }

    @Transactional
    @Override
    public void markAsRead(Integer notificationId) {

        InAppNotification notification =
                inAppNotificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found with ID: "
                                                + notificationId
                                ));

//        Integer currentUserId = currentUserService.getCurrentUserId();

        Integer currentUserId = 1;
        if (!Objects.equals(
                notification.getEmployee().getUserId(),
                currentUserId)) {

            throw new IllegalStateException(
                    "You are not allowed to update this notification"
            );
        }

        notification.setNotificationStatus(
                InAppNotificationStatus.READ
        );

        inAppNotificationRepository.save(notification);
    }

    @Transactional
    @Override
    public void deleteNotification(Integer notificationId) {

        InAppNotification notification =
                inAppNotificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found with ID: "
                                                + notificationId
                                ));

        //        Integer currentUserId = currentUserService.getCurrentUserId();

        Integer currentUserId = 1;

        if (!Objects.equals(
                notification.getEmployee().getUserId(),
                currentUserId)) {

            throw new IllegalStateException(
                    "You are not allowed to delete this notification"
            );
        }

        inAppNotificationRepository.delete(notification);
    }

    @Transactional
    @Override
    public void createNewCommentNotification(Comment comment) {

        Bug bug = comment.getBug();

        User recipient;

        if (bug.getAssignedTo() == null) {
            return;
        }

        if (Objects.equals(
                bug.getAssignedTo().getUserId(),
                comment.getCreatedBy().getUserId())) {

            recipient = bug.getReportedBy();

        } else {

            recipient = bug.getAssignedTo();
        }

        InAppNotification notification =
                InAppNotification.builder()
                        .employee(recipient)
                        .bug(bug)
                        .message(
                                "New comment added to Bug "
                                        + bug.getBugFormatId()
                        )
                        .notificationStatus(
                                InAppNotificationStatus.UNREAD
                        )
                        .createdAt(Instant.now())
                        .build();

        inAppNotificationRepository.save(notification);
    }
}

