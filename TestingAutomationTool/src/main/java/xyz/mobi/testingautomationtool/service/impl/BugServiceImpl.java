package xyz.mobi.testingautomationtool.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.BugDTO.BugRequest;
import xyz.mobi.testingautomationtool.dto.BugDTO.BugResponse;
import xyz.mobi.testingautomationtool.entity.*;
import xyz.mobi.testingautomationtool.enums.BugStatus;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.repository.*;
import xyz.mobi.testingautomationtool.service.BugService;

import java.time.LocalDateTime;

@Service
public class BugServiceImpl implements BugService {

    private final BugRepository bugRepository;
    private final TestCaseRepository testCaseRepository;
    private final FeatureRepository featureRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public BugServiceImpl(
            BugRepository bugRepository,
            TestCaseRepository testCaseRepository,
            FeatureRepository featureRepository,
            UserRepository userRepository,
            NotificationRepository notificationRepository) {
        this.bugRepository = bugRepository;
        this.testCaseRepository = testCaseRepository;
        this.featureRepository = featureRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public BugResponse createBug(BugRequest request) {

        // Step 1: Duplicate guard - verify bugFormatId is unique
        if (bugRepository.existsByBugFormatId(request.getBugFormatId())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE);
        }

        // Step 2: Restrict bug creation status to OPEN only
        if (request.getStatus() != null && request.getStatus() != BugStatus.OPEN) {
            throw new CustomException(ErrorCode.BUSINESS_RULE_VIOLATION);
        }

        // Step 3: Fetch and validate test case
        TestCase testCase = testCaseRepository.findById(request.getTestCaseId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        Integer featureId = request.getFeatureId() != null ? request.getFeatureId() : testCase.getFeatureId();
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // Step 4: Fetch reportedBy user
        User reportedBy = userRepository.findById(request.getReportedBy())
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // Step 5: Fetch optional assignedTo user
        User assignedTo = null;
        if (request.getAssignedTo() != null) {
            assignedTo = userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        // Step 6: Validate reoccurred bug relevance and compute occurrence server-side
        Bug bugReoccurred = null;
        int occurrence = 1;
        if (request.getBugReoccurredId() != null) {
            bugReoccurred = bugRepository.findById(request.getBugReoccurredId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

            // Ensure the reoccurred bug belongs to the same test case
            if (bugReoccurred.getTestCase() != null &&
                    !bugReoccurred.getTestCase().getTestcaseId().equals(testCase.getTestcaseId())) {
                throw new CustomException(ErrorCode.BUSINESS_RULE_VIOLATION);
            }

            int previousOccurrence = bugReoccurred.getBugOccurrence() != null ? bugReoccurred.getBugOccurrence() : 1;
            occurrence = previousOccurrence + 1;
        }

        // Step 7: Build and persist the new Bug entity
        Bug bug = Bug.builder()
                .bugFormatId(request.getBugFormatId())
                .testCase(testCase)
                .feature(feature)
                .title(request.getTitle())
                .description(request.getDescription())
                .severity(request.getSeverity())
                .priority(request.getPriority())
                .status(BugStatus.OPEN)
                .reportedBy(reportedBy)
                .assignedTo(assignedTo)
                .bugReoccurred(bugReoccurred)
                .bugOccurrence(occurrence)
                .build();

        Bug savedBug = bugRepository.save(bug);

        // Step 8: Trigger notification for the assignee if bug is assigned
        if (assignedTo != null) {
            Notification notification = Notification.builder()
                    .employee(assignedTo)
                    .message("New bug assigned: " + savedBug.getTitle() + " (" + savedBug.getBugFormatId() + ")")
                    .bug(savedBug)
                    .createdAt(LocalDateTime.now())
                    .notificationStatus(NotificationStatus.PENDING)
                    .build();
            notificationRepository.save(notification);
        }

        return BugResponse.builder()
                .bugId(savedBug.getBugId())
                .bugFormatId(savedBug.getBugFormatId())
                .testCaseId(savedBug.getTestCase().getTestcaseId())
                .featureId(savedBug.getFeature().getFeatureId())
                .title(savedBug.getTitle())
                .description(savedBug.getDescription())
                .severity(savedBug.getSeverity())
                .priority(savedBug.getPriority())
                .status(savedBug.getStatus())
                .reportedBy(savedBug.getReportedBy().getUserId())
                .assignedTo(savedBug.getAssignedTo() != null ? savedBug.getAssignedTo().getUserId() : null)
                .bugOccurrence(savedBug.getBugOccurrence())
                .build();
    }
}
