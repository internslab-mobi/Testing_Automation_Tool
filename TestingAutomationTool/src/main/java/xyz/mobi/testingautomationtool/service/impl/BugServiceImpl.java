package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugAssignRequest;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugStatusRequest;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.BugRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.BugPutRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.NotificationResponse;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.BugStatus;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;
import xyz.mobi.testingautomationtool.mapper.getMapper.BugMapper;
import xyz.mobi.testingautomationtool.repository.BugRepository;
import xyz.mobi.testingautomationtool.repository.TestCaseRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.BugService;
import xyz.mobi.testingautomationtool.service.EmailService;
import xyz.mobi.testingautomationtool.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BugServiceImpl implements BugService {

    private final BugRepository bugRepository;
    private final UserRepository userRepository;
    private final BugMapper bugMapper;
    private final NotificationService notificationService;
    private final EmailService emailService;

    private final TestCaseRepository testCaseRepository;


    @Override
    public BugResponse createBug(BugRequest request, Integer testCaseId) {

        TestCase testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new RuntimeException("Test case not found"));

//        Integer featureId = request.getFeatureId() != null ? request.getFeatureId() : testCase.getFeatureId();
//        Feature feature = featureRepository.findById(featureId)
//                .orElseThrow(() -> new RuntimeException("Feature not found"));

//        User dummyUser = userRepository.findById(1)
//                .orElseThrow(()-> new RuntimeException("User not found"));


        User reportedBy = userRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User assignedTo = null;
        if (request.getAssignedTo() != null) {
            assignedTo = userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found"));
        }

        Bug bug = new Bug();

        bug.setBugFormatId(request.getBugFormatId());
        bug.setTestCase(testCase);
//        bug.setFeature(feature);
        bug.setTitle(request.getTitle());
        bug.setDescription(request.getDescription());
        bug.setSeverity(request.getSeverity());
        bug.setPriority(request.getPriority());
        bug.setReportedBy(reportedBy);
        bug.setAssignedTo(assignedTo);
        bug.setBugOccurrence(1);

        Bug savedBug = bugRepository.save(bug);


        return BugResponse.builder()
                .bugId(savedBug.getBugId())
                .bugFormatId(savedBug.getBugFormatId())
                .title(savedBug.getTitle())
                .description(savedBug.getDescription())
                .severity(savedBug.getSeverity())
                .priority(savedBug.getPriority())
                .status(savedBug.getStatus())
                .reportedBy(String.valueOf(savedBug.getReportedBy().getUserId()))
                .assignedTo(String.valueOf(savedBug.getAssignedTo() != null ? savedBug.getAssignedTo().getUserId() : null))
                .bugOccurrence(savedBug.getBugOccurrence())
                .build();
    }

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


    @Override
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


    @Override
    public BugResponse getById(Integer bugId) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bug not found with ID: " + bugId));

        return bugMapper.toResponse(bug);
    }


    @Override
    public List<BugResponse> getByAll() {

        return bugRepository.findAll()
                .stream()
                .map(bugMapper::toResponse)
                .toList();
    }

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

    @Override
    @Transactional
    public BugResponse createReoccurrence(
            Integer bugId) {

        Bug oldBug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bug not found with ID: " + bugId));

        Bug latestBug = findLatestOccurrence(oldBug);

        Integer currentOccurrence =
                latestBug.getBugOccurrence() == null
                        ? 1
                        : latestBug.getBugOccurrence();

        Integer nextOccurrence =
                currentOccurrence + 1;


        String bugFormatId =
                generateBugFormatId(
                        latestBug.getTestCase()
                                .getTestcaseId());


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

                .status(BugStatus.OPEN)

                .reportedBy(
                        latestBug.getReportedBy())

                .assignedTo(
                        latestBug.getAssignedTo())

                .resolvedAt(null)

                .bugReoccurred(
                        latestBug)

                .bugOccurrence(
                        nextOccurrence)

                .active(true)

                .build();

        newBug = bugRepository.save(newBug);

        return bugMapper.toResponse(newBug);
    }

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