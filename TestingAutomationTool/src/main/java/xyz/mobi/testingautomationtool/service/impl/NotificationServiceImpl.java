package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.NotificationRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.NotificationResponse;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.Notification;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.getMapper.NotificationMapper;
import xyz.mobi.testingautomationtool.repository.BugRepository;
import xyz.mobi.testingautomationtool.repository.NotificationRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.EmailService;
import xyz.mobi.testingautomationtool.service.NotificationService;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final BugRepository bugRepository;
    private final NotificationMapper notificationMapper;

    private final EmailService emailService;

    @Transactional
    @Override
    public NotificationResponse createNotification(
            NotificationRequest request) {

        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with ID: "
                                        + request.getEmployeeId()));

        User assigned = userRepository.findById(request.getAssignedId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Assigned user not found with ID: "
                                        + request.getAssignedId()));

        Bug bug = bugRepository.findById(request.getBugId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bug not found with ID: "
                                        + request.getBugId()));

        Notification notification = Notification.builder()
                .employee(employee)
                .assigned(assigned)
                .bug(bug)
                .createdAt(Instant.now())
                .notificationStatus(NotificationStatus.PENDING)
                .build();

        notification = notificationRepository.save(notification);

        try {

            emailService.sendBugAssignedEmail(
                    assigned.getEmail(),
                    bug
            );

            notification.setNotificationStatus(
                    NotificationStatus.SENT
            );

        } catch (Exception ex) {

            notification.setNotificationStatus(
                    NotificationStatus.FAILED
            );
        }

        notificationRepository.save(notification);

        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public void createReassignNotification(
            Integer oldAssignedId,
            Integer newAssignedId,
            Integer bugId) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bug not found with ID: " + bugId));

        if (bug.getUpdatedBy() == null) {
            throw new ResourceNotFoundException(
                    "Updated-by user not found for bug ID: " + bugId);
        }

        User employee = bug.getUpdatedBy();


        // OLD ASSIGNEE
        if (oldAssignedId != null) {

            User oldAssignee = userRepository.findById(oldAssignedId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Old assignee not found with ID: "
                                            + oldAssignedId));

            NotificationRequest oldRequest =
                    NotificationRequest.builder()
                            .employeeId(employee.getUserId())
                            .assignedId(oldAssignedId)
                            .bugId(bugId)
                            .build();

            User oldEmployee = userRepository.findById(
                    oldRequest.getEmployeeId()
            ).orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Employee not found with ID: "
                                    + oldRequest.getEmployeeId()));

            Notification oldNotification = Notification.builder()
                    .employee(oldEmployee)
                    .assigned(oldAssignee)
                    .bug(bug)
                    .createdAt(Instant.now())
                    .notificationStatus(NotificationStatus.PENDING)
                    .build();

            oldNotification = notificationRepository.save(oldNotification);

            try {

                emailService.sendBugReassignedEmail(
                        oldAssignee.getEmail(),
                        bug
                );

                oldNotification.setNotificationStatus(
                        NotificationStatus.REASSIGNED
                );

            } catch (Exception ex) {

                oldNotification.setNotificationStatus(
                        NotificationStatus.FAILED
                );
            }

            notificationRepository.save(oldNotification);
        }


        // NEW ASSIGNEE
        if (newAssignedId != null) {

            User newAssignee = userRepository.findById(newAssignedId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "New assignee not found with ID: "
                                            + newAssignedId));

            NotificationRequest newRequest =
                    NotificationRequest.builder()
                            .employeeId(employee.getUserId())
                            .assignedId(newAssignedId)
                            .bugId(bugId)
                            .build();

            User newEmployee = userRepository.findById(
                    newRequest.getEmployeeId()
            ).orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Employee not found with ID: "
                                    + newRequest.getEmployeeId()));

            Notification newNotification = Notification.builder()
                    .employee(newEmployee)
                    .assigned(newAssignee)
                    .bug(bug)
                    .createdAt(Instant.now())
                    .notificationStatus(NotificationStatus.PENDING)
                    .build();

            newNotification = notificationRepository.save(newNotification);

            try {

                emailService.sendBugAssignedEmail(
                        newAssignee.getEmail(),
                        bug
                );

                newNotification.setNotificationStatus(
                        NotificationStatus.SENT
                );

            } catch (Exception ex) {

                newNotification.setNotificationStatus(
                        NotificationStatus.FAILED
                );

            }

            notificationRepository.save(newNotification);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByAssignedId(
            Integer assignedId) {

        userRepository.findById(assignedId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: "
                                        + assignedId
                        )
                );

        return notificationRepository
                .findByEmployee_UserIdOrderByCreatedAtDesc(
                        assignedId
                )
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByAll() {

        return notificationRepository
                .findAll()
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteNotification(Integer notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}