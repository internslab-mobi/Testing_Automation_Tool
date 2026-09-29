package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.BugDTO.BugRequest;
import xyz.mobi.testingautomationtool.dto.BugDTO.BugResponse;
import xyz.mobi.testingautomationtool.entity.*;
import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.repository.*;
import xyz.mobi.testingautomationtool.service.BugService;
import xyz.mobi.testingautomationtool.service.EmailService;
import xyz.mobi.testingautomationtool.service.NotificationService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BugServiceImpl implements BugService {

    private final BugRepository bugRepository;
    private final TestCaseRepository testCaseRepository;
    private final FeatureRepository featureRepository;
    private final UserRepository userRepository;
    private final BugHistoryRepository bugHistoryRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Override
    @Transactional
    public BugResponse createBug(BugRequest request) {

        // Step 1: Duplicate guard - verify bugFormatId is unique
      // noneed  if (bugRepository.existsByBugFormatId(request.getBugFormatId())) {throw new CustomException(ErrorCode.DUPLICATE_RESOURCE);}

        // Step 2: Restrict bug creation status to OPEN only
        if (request.getStatus() != null && request.getStatus() != BugStatus.OPEN) {
            throw new CustomException(ErrorCode.BUSINESS_RULE_VIOLATION);
        }

        // Step 3: Fetch and validate test case
        TestCase testCase = testCaseRepository.findById(request.getTestCaseId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (testCase.isDeleted()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        // Resolve Feature
        Integer featureId = request.getFeatureId();
        if (featureId == null && testCase.getFeature() != null) {
            featureId = testCase.getFeature().getFeatureId();
        }
        if (featureId == null) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        Feature feature = featureRepository.findByFeatureIdForUpdate(featureId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        String featureName = feature.getFeatureName()
                .trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_");

        String prefix = "BUG-" + featureName + "-";

        int nextNumber = 1;

        Optional<Bug> latestBug =
                bugRepository.findTopByFeature_FeatureIdAndBugFormatIdStartingWithOrderByBugFormatIdDesc(
                        featureId,
                        prefix
                );

        if (latestBug.isPresent()) {
            String latestFormatId = latestBug.get().getBugFormatId();

            String numberPart = latestFormatId.substring(prefix.length());

            nextNumber = Integer.parseInt(numberPart) + 1;
        }

        String bugFormatId = prefix + String.format("%03d", nextNumber);


        // Step 4: Fetch reporter
        User reporter = userRepository.findById(request.getReportedBy())
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // Step 5: Fetch optional assigned developer
        User assignedTo = null;
        if (request.getAssignedTo() != null) {
            assignedTo = userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        // Step 6: Validate re-occurrence and compute occurrence number
        int occurrence = 1;
        if (request.getBugReoccurredId() != null) {
            Bug previousBug = bugRepository.findById(request.getBugReoccurredId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

            // Ensure the previous bug belongs to the same test case
            if (previousBug.getTestCase() == null ||
                    !previousBug.getTestCase().getTestcaseId().equals(testCase.getTestcaseId())) {
                throw new CustomException(ErrorCode.BUSINESS_RULE_VIOLATION);
            }

            int prevOccurrence = previousBug.getBugOccurrence() != null ? previousBug.getBugOccurrence() : 1;
            occurrence = prevOccurrence + 1;
        }

        // Step 7: Apply defaults and build Bug entity
        BugSeverity severity = request.getSeverity() != null ? request.getSeverity() : BugSeverity.MEDIUM;
        BugPriority priority = request.getPriority() != null ? request.getPriority() : BugPriority.MEDIUM;
        BugCategory category = request.getCategory() != null ? request.getCategory() : BugCategory.PRE_PRODUCTION;

        Bug bug = Bug.builder()
                .bugFormatId(bugFormatId)
                .testCase(testCase)
                .feature(feature)
                .title(request.getTitle())
                .description(request.getDescription())
                .severity(severity)
                .priority(priority)
                .category(category)
                .status(BugStatus.OPEN)
                .reportedBy(reporter)
                .updatedBy(reporter)
                .assignedTo(assignedTo)
                .bugOccurrence(occurrence)
                .comments(request.getComments())
                .dynamicFields(request.getDynamicFields() != null ? request.getDynamicFields() : new HashMap<>())
                .isActive(true)
                .isDeleted(false)
                .build();

        Bug savedBug = bugRepository.save(bug);

        // Step 8: Create initial BugHistory audit record
        BugHistory history = BugHistory.builder()
                .bug(savedBug)
                .executedBy(reporter.getUserId())
                .bugStatus(BugStatus.OPEN)
                .assignedTo(assignedTo != null ? assignedTo.getUserId() : null)
                .createdAt(Instant.now())
                .build();
        bugHistoryRepository.save(history);

        // Step 9: Notification and email dispatch if assigned
        if (assignedTo != null) {
            Notification notification = notificationService.createNotification(reporter, assignedTo, savedBug);
            try {
                if (assignedTo.getEmail() != null && !assignedTo.getEmail().isBlank()) {
                    emailService.sendBugAssignmentEmail(assignedTo.getEmail(), savedBug);
                    notificationService.updateNotificationStatus(notification.getNotificationId(), NotificationStatus.SENT);
                } else {
                    log.warn("Assigned developer {} has no email configured", assignedTo.getUsername());
                    notificationService.updateNotificationStatus(notification.getNotificationId(), NotificationStatus.FAIL);
                }
            } catch (Exception e) {
                log.error("Failed to send assignment email for bug {}: {}", savedBug.getBugFormatId(), e.getMessage());
                notificationService.updateNotificationStatus(notification.getNotificationId(), NotificationStatus.FAIL);
            }
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
                .category(savedBug.getCategory())
                .status(savedBug.getStatus())
                .reportedBy(savedBug.getReportedBy().getUserId())
                .assignedTo(savedBug.getAssignedTo() != null ? savedBug.getAssignedTo().getUserId() : null)
                .bugOccurrence(savedBug.getBugOccurrence())
                .comments(savedBug.getComments())
                .createdAt(savedBug.getCreatedAt())
                .updatedAt(savedBug.getUpdatedAt())
                .build();
    }
}
