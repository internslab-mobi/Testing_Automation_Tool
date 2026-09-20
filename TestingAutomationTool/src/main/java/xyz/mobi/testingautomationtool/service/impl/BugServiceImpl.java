package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugAssignRequest;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugStatusRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.BugPutRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.NotificationResponse;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.BugStatus;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;
import xyz.mobi.testingautomationtool.mapper.getMapper.BugMapper;
import xyz.mobi.testingautomationtool.repository.BugRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.BugService;
import xyz.mobi.testingautomationtool.service.EmailService;
import xyz.mobi.testingautomationtool.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BugServiceImpl implements BugService {

    private final BugRepository bugRepository;
    private final UserRepository userRepository;
    private final BugMapper bugMapper;
    private final NotificationService notificationService;
    private final EmailService emailService;


    // =========================================================
    // 1. SOFT DELETE
    // PATCH /bugs/{bugId}/delete
    // =========================================================

    @Override
    @Transactional
    public String softDeleteBug(Integer bugId) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bug not found with ID: " + bugId));

        bug.setActive(false);

        bugRepository.save(bug);

        return "Bug deleted successfully";
    }


    // =========================================================
    // 2. HARD DELETE
    // DELETE /bugs/{bugId}
    // =========================================================

    @Override
    @Transactional
    public String hardDeleteBug(Integer bugId) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bug not found with ID: " + bugId));

        bugRepository.delete(bug);

        return "Bug permanently deleted";
    }


    // =========================================================
    // 3. FULL UPDATE
    // PUT /bugs/{bugId}
    // =========================================================

    @Override
    @Transactional
    public BugResponse updateBug(
            Integer bugId,
            BugPutRequest request) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bug not found with ID: " + bugId));

        bug.setTitle(request.getTitle());
        bug.setDescription(request.getDescription());
        bug.setPriority(request.getPriority());
        bug.setSeverity(request.getSeverity());
        bug.setStatus(request.getStatus());

        /*
         * If PUT changes the status to RESOLVED,
         * update resolved_at automatically.
         */
        if (request.getStatus() == BugStatus.RESOLVED) {

            if (bug.getResolvedAt() == null) {
                bug.setResolvedAt(LocalDateTime.now());
            }

        } else {

            bug.setResolvedAt(null);
        }

        bug = bugRepository.save(bug);

        return bugMapper.toResponse(bug);
    }


    // =========================================================
    // 4. ASSIGN USER
    // PATCH /bugs/{bugId}/assign
    // =========================================================

    @Override
    @Transactional
    public BugResponse assignBug(
            Integer bugId,
            BugAssignRequest request) {

        // 1. Find the bug
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bug not found with ID: " + bugId));

        // 2. Find the assigned user
        User assignedUser = userRepository
                .findById(request.getAssignedTo())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with ID: "
                                        + request.getAssignedTo()));

        // 3. Assign the user to the bug
        bug.setAssignedTo(assignedUser);

        // 4. Save the updated bug
        bug = bugRepository.save(bug);

        // 5. Prepare notification message
        String message =
                "Bug " + bug.getBugFormatId()
                        + " has been assigned to you.";

        // 6. Save notification in the database
        NotificationResponse notification = notificationService.createNotification(
                assignedUser.getUserId(),
                bug.getBugId(),
                message
        );

        // 7. Send email with bug and testcase details
        try {

            emailService.sendBugAssignmentEmail(
                    assignedUser.getEmail(),
                    bug
            );

            notificationService.updateStatus(
                    notification.getNotificationId(),
                    NotificationStatus.SENT
            );

        } catch (Exception exception) {

            notificationService.updateStatus(
                    notification.getNotificationId(),
                    NotificationStatus.FAIL
            );
        }

        // 8. Return updated bug response
        return bugMapper.toResponse(bug);
    }


    // =========================================================
    // 5. CHANGE STATUS
    // PATCH /bugs/{bugId}/status
    // =========================================================

    @Override
    @Transactional
    public BugResponse updateStatus(
            Integer bugId,
            BugStatusRequest request) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bug not found with ID: " + bugId));

        BugStatus newStatus = request.getStatus();

        bug.setStatus(newStatus);

        if (newStatus == BugStatus.RESOLVED) {

            bug.setResolvedAt(LocalDateTime.now());

        } else {

            bug.setResolvedAt(null);
        }

        bug = bugRepository.save(bug);

        return bugMapper.toResponse(bug);
    }


    // =========================================================
    // 6. GET BY ID
    // GET /bugs/{bugId}
    // =========================================================

    @Override
    public BugResponse getById(Integer bugId) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bug not found with ID: " + bugId));

        return bugMapper.toResponse(bug);
    }


    // =========================================================
    // 7. GET ALL
    // GET /bugs
    // =========================================================

    @Override
    public List<BugResponse> getByAll() {

        return bugRepository.findAll()
                .stream()
                .map(bugMapper::toResponse)
                .toList();
    }


    // =========================================================
    // 8. GET BY TESTCASE ID
    // GET /bugs/testcase/{testcaseId}
    // =========================================================

    @Override
    public List<BugResponse> getByTestcaseId(
            Integer testcaseId) {

        return bugRepository
                .findByTestCase_TestcaseIdOrderByBugIdAsc(
                        testcaseId)
                .stream()
                .map(bugMapper::toResponse)
                .toList();
    }

    @Override
    public List<BugResponse> getByFeatureId(
            Integer featureId) {

        return bugRepository
                .findByFeature_FeatureIdOrderByBugIdAsc(featureId)
                .stream()
                .map(bugMapper::toResponse)
                .toList();
    }


    // =========================================================
    // 9. RE-OCCURRENCE
    // POST /bugs/{bugId}/reoccurrence
    // =========================================================

    @Override
    @Transactional
    public BugResponse createReoccurrence(
            Integer bugId) {

        Bug oldBug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bug not found with ID: " + bugId));

        /*
         * Find the latest bug in the occurrence chain.
         *
         * Example:
         *
         * BUG-000001
         *      ↓
         * BUG-000002
         *      ↓
         * BUG-000003
         */

        Bug latestBug = findLatestOccurrence(oldBug);

        Integer currentOccurrence =
                latestBug.getBugOccurrence() == null
                        ? 1
                        : latestBug.getBugOccurrence();

        Integer nextOccurrence =
                currentOccurrence + 1;


        /*
         * Generate next Bug Format ID
         * for this testcase.
         */

        String bugFormatId =
                generateBugFormatId(
                        latestBug.getTestCase()
                                .getTestcaseId());


        /*
         * Copy the bug details.
         */

        Bug newBug = Bug.builder()
                .bugFormatId(bugFormatId)

                .testCase(
                        latestBug.getTestCase())

                .feature(
                        latestBug.getFeature())

                .title(
                        latestBug.getTitle())

                .description(
                        latestBug.getDescription())

                .severity(
                        latestBug.getSeverity())

                .priority(
                        latestBug.getPriority())

                /*
                 * New occurrence starts as OPEN.
                 */
                .status(BugStatus.OPEN)

                .reportedBy(
                        latestBug.getReportedBy())

                .assignedTo(
                        latestBug.getAssignedTo())

                .resolvedAt(null)

                /*
                 * Point to previous occurrence.
                 */
                .bugReoccurred(
                        latestBug)

                .bugOccurrence(
                        nextOccurrence)

                .active(true)

                .build();

        newBug = bugRepository.save(newBug);

        return bugMapper.toResponse(newBug);
    }


    // =========================================================
    // FIND LATEST OCCURRENCE
    // =========================================================

    private Bug findLatestOccurrence(Bug bug) {

        Bug current = bug;

        while (true) {

            Optional<Bug> nextBug =
                    bugRepository.findByBugReoccurred(current);

            if (nextBug.isEmpty()) {
                return current;
            }

            current = nextBug.get();
        }
    }


    // =========================================================
    // GENERATE BUG FORMAT ID
    // =========================================================

    private String generateBugFormatId(
            Integer testcaseId) {

        Optional<Bug> latestBug =
                bugRepository
                        .findTopByTestCase_TestcaseIdOrderByBugIdDesc(
                                testcaseId);

        int nextNumber = latestBug
                .map(bug -> {

                    String bugFormatId =
                            bug.getBugFormatId();

                    String numberPart =
                            bugFormatId.substring(
                                    bugFormatId.lastIndexOf("-") + 1);

                    return Integer.parseInt(numberPart) + 1;
                })
                .orElse(1);

        return String.format(
                "BUG-%03d",
                nextNumber);
    }
}