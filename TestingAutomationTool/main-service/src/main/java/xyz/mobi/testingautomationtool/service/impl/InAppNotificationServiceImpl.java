package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.NotificationDTO.*;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.Comment;
import xyz.mobi.testingautomationtool.entity.InAppNotification;
import xyz.mobi.testingautomationtool.entity.Project;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.BugStatus;
import xyz.mobi.testingautomationtool.enums.InAppNotificationStatus;
import xyz.mobi.testingautomationtool.enums.UserRole;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.NotificationMapper;
import xyz.mobi.testingautomationtool.repository.InAppNotificationRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.AuthService;
import xyz.mobi.testingautomationtool.service.InAppNotificationService;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InAppNotificationServiceImpl implements InAppNotificationService {

    private final InAppNotificationRepository inAppNotificationRepository;
    private final NotificationMapper notificationMapper;
    private final AuthService authService;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public void createBugFixedNotification(Bug bug) {
        if (bug == null || bug.getReportedBy() == null) {
            return;
        }

        String msg = bug.getStatus() == BugStatus.NOT_A_BUG
                ? "Bug " + bug.getBugFormatId() + " was marked as NOT A BUG by developer"
                : "Bug " + bug.getBugFormatId() + " has been fixed by developer";

        InAppNotification notification = InAppNotification.builder()
                .employee(bug.getReportedBy())
                .bug(bug)
                .message(msg)
                .notificationStatus(InAppNotificationStatus.UNREAD)
                .createdAt(Instant.now())
                .build();

        inAppNotificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InAppNotificationResponse> getMyNotifications() {
        Integer currentUserId = authService.getCurrentUser().getUserId();

        return inAppNotificationRepository
                .findByEmployee_UserIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(notificationMapper::toInAppResponse)
                .toList();
    }

    @Transactional
    @Override
    public void markAsRead(Integer notificationId) {
        InAppNotification notification = inAppNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        Integer currentUserId = authService.getCurrentUser().getUserId();
        if (!Objects.equals(notification.getEmployee().getUserId(), currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not allowed to update this notification");
        }

        notification.setNotificationStatus(InAppNotificationStatus.READ);
        inAppNotificationRepository.save(notification);
    }

    @Transactional
    @Override
    public String patchNotification(Integer notificationId, xyz.mobi.testingautomationtool.dto.NotificationDTO.NotificationPatchRequest request) {
        InAppNotification notification = inAppNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        Integer currentUserId = authService.getCurrentUser().getUserId();
        if (!Objects.equals(notification.getEmployee().getUserId(), currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not allowed to update this notification");
        }

        InAppNotificationStatus newStatus = (request != null && request.getStatus() != null)
                ? request.getStatus()
                : InAppNotificationStatus.READ;

        notification.setNotificationStatus(newStatus);
        inAppNotificationRepository.save(notification);

        return "Notification with ID " + notificationId + " updated successfully to status " + newStatus;
    }

    @Transactional
    @Override
    public void deleteNotification(Integer notificationId) {
        InAppNotification notification = inAppNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        Integer currentUserId = authService.getCurrentUser().getUserId();
        if (!Objects.equals(notification.getEmployee().getUserId(), currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not allowed to delete this notification");
        }

        inAppNotificationRepository.delete(notification);
    }

    @Transactional
    @Override
    public void createNewCommentNotification(Comment comment) {
        if (comment == null || comment.getBug() == null) {
            return;
        }

        Bug bug = comment.getBug();
        if (bug.getAssignedTo() == null) {
            return;
        }

        User recipient;
        if (comment.getCreatedBy() != null && Objects.equals(bug.getAssignedTo().getUserId(), comment.getCreatedBy().getUserId())) {
            recipient = bug.getReportedBy();
        } else {
            recipient = bug.getAssignedTo();
        }

        if (recipient == null) {
            return;
        }

        InAppNotification notification = InAppNotification.builder()
                .employee(recipient)
                .bug(bug)
                .message("New comment added to Bug " + bug.getBugFormatId())
                .notificationStatus(InAppNotificationStatus.UNREAD)
                .createdAt(Instant.now())
                .build();

        inAppNotificationRepository.save(notification);
    }

    @Transactional
    @Override
    public void createProjectInactiveNotification(Project project) {
        if (project == null) {
            return;
        }

        List<User> targetUsers = userRepository.findAllByRoleRoleInAndIsActiveTrue(
                List.of(UserRole.TESTER.name(), UserRole.MANAGER.name()));

        if (targetUsers == null || targetUsers.isEmpty()) {
            return;
        }

        String projectName = project.getProjectName() != null ? project.getProjectName() : "ID #" + project.getProjectId();
        String message = "Project '" + projectName + "' has been set to INACTIVE";
        Instant now = Instant.now();

        List<InAppNotification> notifications = targetUsers.stream()
                .map(user -> InAppNotification.builder()
                        .employee(user)
                        .bug(null)
                        .message(message)
                        .notificationStatus(InAppNotificationStatus.UNREAD)
                        .createdAt(now)
                        .build())
                .toList();

        inAppNotificationRepository.saveAll(notifications);
    }
}
