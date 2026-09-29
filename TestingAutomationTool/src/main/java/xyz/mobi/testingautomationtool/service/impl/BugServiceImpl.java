package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugPatchRequest;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.DeveloperBugStatusRequest;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.NotificationRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.BugPutRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.BugStatus;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.postMapper.BugMapper;
import xyz.mobi.testingautomationtool.repository.BugRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.BugService;
import xyz.mobi.testingautomationtool.service.NotificationService;
import xyz.mobi.testingautomationtool.utils.Utils;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class BugServiceImpl implements BugService {

    private final BugRepository bugRepository;
    private final UserRepository userRepository;
    private final BugMapper bugMapper;
    private final NotificationService notificationService;
    private final Utils utils;


    @Override
    public BugResponse updateBug(Integer bugId, BugPutRequest request) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Bug not found with ID: " + bugId));

        if (!bug.isActive()) {
            throw new IllegalStateException(
                    "Cannot update disabled bug with ID: " + bugId);
        }

        if (bug.isDeleted()) {
            throw new IllegalStateException(
                    "Cannot update deleted bug with ID: " + bugId);
        }

        Integer oldAssignedUserId =
                bug.getAssignedTo() != null
                        ? bug.getAssignedTo().getUserId()
                        : null;

        BugStatus oldStatus = bug.getStatus();

        // dummy user
        User updatedBy = userRepository.findById(1)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Updated user not found"));

        //well same as the above dummy user
        User executedBy = bug.getExecutedBy();

        if (!Objects.equals(oldStatus, request.getStatus())) {

            executedBy = userRepository.findById(1).orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Executed user not found"));

            executedBy = updatedBy;
        }

        User assignedTo = bug.getAssignedTo();

        if (request.getAssignedTo() != null) {

            assignedTo = userRepository.findById(
                    request.getAssignedTo()
            ).orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Assigned user not found with ID: "
                                    + request.getAssignedTo()
                    ));
        }

        bugMapper.updateEntity(bug, request);

        bug.setExecutedBy(executedBy);
        bug.setAssignedTo(assignedTo);
        bug.setUpdatedBy(updatedBy);

        if(assignedTo != null) {
            bug.setStatus(BugStatus.IN_PROGRESS);
        }


        if (request.getStatus() == BugStatus.RESOLVED) {
            if (bug.getResolvedAt() == null) {
                bug.setResolvedAt(Instant.now());
            }
        } else {
            bug.setResolvedAt(null);
        }

        // Determine changes
        boolean statusChanged =
                !Objects.equals(oldStatus, bug.getStatus());

        Integer newAssignedUserId =
                bug.getAssignedTo() != null
                        ? bug.getAssignedTo().getUserId()
                        : null;

        boolean assignmentChanged =
                !Objects.equals(
                        oldAssignedUserId,
                        newAssignedUserId
                );

        bug = bugRepository.save(bug);

        // Create history
        if (statusChanged || assignmentChanged) {

            utils.bugHistory(
                    bug,
                    bug.getExecutedBy() != null
                            ? bug.getExecutedBy()
                            : updatedBy
            );
        }


        if (assignmentChanged && newAssignedUserId != null) {

            if (oldAssignedUserId == null) {
                // First-time assignment
                notificationService.createNotification(
                        NotificationRequest.builder()
                                .employeeId(bug.getUpdatedBy().getUserId())
                                .assignedId(newAssignedUserId)
                                .bugId(bug.getBugId())
                                .build()
                );

            } else {
                // Reassignment: old → new
                notificationService.createReassignNotification(
                        oldAssignedUserId,
                        newAssignedUserId,
                        bug.getBugId()
                );
            }
        }

        return bugMapper.toResponse(bug);

    }

    @Override
    public BugResponse patchBug(Integer bugId, BugPatchRequest request) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bug not found with ID: " + bugId
                        ));

        if (!bug.isActive()) {
            throw new IllegalStateException(
                    "Cannot update disabled bug with ID: " + bugId
            );
        }

        if (bug.isDeleted()) {
            throw new IllegalStateException(
                    "Cannot update deleted bug with ID: " + bugId
            );
        }

        if (request.getAssignment() == null
                && request.getStatus() == null) {

            throw new IllegalArgumentException(
                    "At least one patch operation is required"
            );
        }

        Integer oldAssignedUserId =
                bug.getAssignedTo() != null
                        ? bug.getAssignedTo().getUserId()
                        : null;

        BugStatus oldStatus = bug.getStatus();


        User currentUser = userRepository.findById(1)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Current user not found"
                        ));

        boolean assignmentChanged = false;
        boolean statusChanged = false;


        if (request.getAssignment() != null) {

            Integer assignedUserId =
                    request.getAssignment().getAssignedTo();

            User assignedUser =
                    userRepository.findById(assignedUserId)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "User not found with ID: "
                                                    + assignedUserId
                                    ));

            assignmentChanged =
                    !Objects.equals(
                            oldAssignedUserId,
                            assignedUserId
                    );

            bugMapper.updateAssignment(
                    request.getAssignment(),
                    bug
            );

            bug.setAssignedTo(assignedUser);
        }


        if (request.getStatus() != null) {

            BugStatus newStatus =
                    request.getStatus().getStatus();

            statusChanged =
                    !Objects.equals(
                            oldStatus,
                            newStatus
                    );

            bugMapper.updateStatus(
                    request.getStatus(),
                    bug
            );

            if (statusChanged) {
                bug.setExecutedBy(currentUser);
            }

            // RESOLVED DATE
            if (newStatus == BugStatus.RESOLVED) {

                if (bug.getResolvedAt() == null) {
                    bug.setResolvedAt(Instant.now());
                }

            } else {
                bug.setResolvedAt(null);
            }
        }


        bug.setUpdatedBy(currentUser);

        bug = bugRepository.save(bug);


        if (assignmentChanged || statusChanged) {

            utils.bugHistory(
                    bug,
                    statusChanged
                            ? bug.getExecutedBy()
                            : currentUser
            );
        }


        if (assignmentChanged) {

            Integer newAssignedUserId =
                    bug.getAssignedTo() != null
                            ? bug.getAssignedTo().getUserId()
                            : null;

            if (newAssignedUserId != null) {

                if (oldAssignedUserId == null) {

                    notificationService.createNotification(
                            NotificationRequest.builder()
                                    .employeeId(
                                            currentUser.getUserId()
                                    )
                                    .assignedId(
                                            newAssignedUserId
                                    )
                                    .bugId(
                                            bug.getBugId()
                                    )
                                    .build()
                    );

                } else {

                    notificationService.createReassignNotification(
                            oldAssignedUserId,
                            newAssignedUserId,
                            bug.getBugId()
                    );
                }
            }
        }

        return bugMapper.toResponse(bug);
    }

    @Override
    public void softDeleteBug(Integer bugId) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bug not found with ID: " + bugId
                        ));

        if (bug.isDeleted()) {
            throw new IllegalStateException(
                    "Bug is already deleted with ID: " + bugId
            );
        }

        if (!bug.isActive()) {
            throw new IllegalStateException(
                    "Bug is already inactive with ID: " + bugId
            );
        }

        // Current logged-in user
        User currentUser = userRepository.findById(1)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Current user not found"
                        ));

        bug.setActive(false);
        bug.setDeleted(true);
        bug.setUpdatedBy(currentUser);

        bugRepository.save(bug);
    }

    @Override
    @Transactional
    public void hardDeleteBug(Integer bugId) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bug not found with ID: " + bugId
                        ));

        bugRepository.delete(bug);
    }

    @Override
    public BugResponse updateDeveloperStatus(
            Integer bugId,
            DeveloperBugStatusRequest request) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bug not found with ID: " + bugId
                        ));

        if (!bug.isActive()) {
            throw new IllegalStateException(
                    "Cannot update disabled bug with ID: " + bugId
            );
        }

        if (bug.isDeleted()) {
            throw new IllegalStateException(
                    "Cannot update deleted bug with ID: " + bugId
            );
        }

        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException(
                    "Developer status is required"
            );
        }

        if (bug.getAssignedTo() == null) {
            throw new IllegalStateException(
                    "Bug is not assigned to any developer"
            );
        }

        User currentUser = userRepository.findById(1)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Current user not found"
                        ));

        if (!Objects.equals(
                bug.getAssignedTo().getUserId(),
                currentUser.getUserId())) {

            throw new IllegalStateException(
                    "You are not assigned to this bug"
            );
        }

        BugStatus oldStatus = bug.getStatus();

        BugStatus newStatus = switch (request.getStatus()) {

            case OPEN ->
                    BugStatus.IN_PROGRESS;

            case FIXED ->
                    BugStatus.FIXED;

            case NOT_A_BUG ->
                    BugStatus.NOT_A_BUG;
        };

        boolean statusChanged =
                !Objects.equals(oldStatus, newStatus);

        if (!statusChanged) {
            return bugMapper.toResponse(bug);
        }

        bug.setStatus(newStatus);
        bug.setExecutedBy(currentUser);
        bug.setUpdatedBy(currentUser);

        if (newStatus == BugStatus.RESOLVED) {

            if (bug.getResolvedAt() == null) {
                bug.setResolvedAt(Instant.now());
            }

        } else {
            bug.setResolvedAt(null);
        }

        bug = bugRepository.save(bug);

        utils.bugHistory(
                bug,
                currentUser
        );

        return bugMapper.toResponse(bug);
    }
}