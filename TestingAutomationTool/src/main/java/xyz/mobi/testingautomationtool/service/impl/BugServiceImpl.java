package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.BugDto.*;
import xyz.mobi.testingautomationtool.entity.*;
import xyz.mobi.testingautomationtool.enums.*;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.BugMapper;
import xyz.mobi.testingautomationtool.repository.*;
import xyz.mobi.testingautomationtool.service.AuthService;
import xyz.mobi.testingautomationtool.service.BugService;
import xyz.mobi.testingautomationtool.service.EmailService;
import xyz.mobi.testingautomationtool.service.NotificationService;
import xyz.mobi.testingautomationtool.specification.BugSpecification;

import java.time.*;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BugServiceImpl implements BugService {

    private final BugRepository bugRepository;
    private final TestCaseRepository testCaseRepository;
    private final FeatureRepository featureRepository;
    private final UserRepository userRepository;
    private final BugHistoryRepository bugHistoryRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final BugMapper bugMapper;
    private final AuthService authService;

    @Override
    public BugResponse createBug(BugRequest request) {
        // Step 1: Duplicate guard - verify bugFormatId is unique
        if (request.getBugFormatId() != null && bugRepository.existsByBugFormatId(request.getBugFormatId())) {
            throw new IllegalArgumentException("Bug with format ID '" + request.getBugFormatId() + "' already exists");
        }

        // Step 2: Restrict bug creation status to OPEN only
        if (request.getStatus() != null && request.getStatus() != BugStatus.OPEN) {
            throw new IllegalArgumentException("New bug can only be created with OPEN status, received: " + request.getStatus());
        }

        // Step 3: Fetch and validate test case
        TestCase testCase = testCaseRepository.findById(request.getTestCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Test case not found with id: " + request.getTestCaseId()));

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

        String bugFormatId = request.getBugFormatId();
        if (bugFormatId == null || bugFormatId.isBlank()) {
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
                try {
                    String numberPart = latestFormatId.substring(prefix.length());
                    nextNumber = Integer.parseInt(numberPart) + 1;
                } catch (Exception ignored) {
                }
            }
            bugFormatId = prefix + String.format("%03d", nextNumber);
        }

        // Step 4: Fetch reporter
        User reporter;
        if (request.getReportedBy() != null) {
            reporter = userRepository.findById(request.getReportedBy())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        } else {
            reporter = authService.getCurrentUser();
        }

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
                    notificationService.updateNotificationStatus(notification.getNotificationId(), NotificationStatus.FAILED);
                }
            } catch (Exception e) {
                log.error("Failed to send assignment email for bug {}: {}", savedBug.getBugFormatId(), e.getMessage());
                notificationService.updateNotificationStatus(notification.getNotificationId(), NotificationStatus.FAILED);
            }
        }

        return bugMapper.toResponse(savedBug);
    }

    @Override
    @Transactional(readOnly = true)
    public BugResponse getById(Integer bugId) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with ID: " + bugId));

        if (!bug.isActive() || bug.isDeleted()) {
            throw new ResourceNotFoundException("The bug has been removed with ID: " + bugId);
        }

        return bugMapper.toResponse(bug);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BugResponse> getAllBugs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bugRepository.findByIsActiveTrue(pageable)
                .map(bugMapper::toResponse);
    }

    @Override
    public BugResponse updateBug(Integer bugId, BugPutRequest request) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with ID: " + bugId));

        if (bug.isDeleted()) {
            throw new ResourceNotFoundException("Bug is deleted with ID: " + bugId);
        }

        bugMapper.updateEntity(bug, request);
        bug.setUpdatedBy(authService.getCurrentUser());
        Bug saved = bugRepository.save(bug);
        return bugMapper.toResponse(saved);
    }

    @Override
    public BugResponse patchBug(Integer bugId, BugPatchRequest request) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with ID: " + bugId));

        if (bug.isDeleted()) {
            throw new ResourceNotFoundException("Bug is deleted with ID: " + bugId);
        }

        bugMapper.patchEntity(bug, request);
        bug.setUpdatedBy(authService.getCurrentUser());
        Bug saved = bugRepository.save(bug);
        return bugMapper.toResponse(saved);
    }

    @Override
    public BugResponse assignBug(Integer bugId, BugAssignRequest request) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with ID: " + bugId));

        User newAssignee = userRepository.findById(request.getAssignedTo())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getAssignedTo()));

        User previousAssignee = bug.getAssignedTo();
        bug.setAssignedTo(newAssignee);
        bug.setUpdatedBy(authService.getCurrentUser());
        Bug saved = bugRepository.save(bug);

        if (previousAssignee != null && previousAssignee.getEmail() != null) {
            emailService.sendBugReassignedEmail(previousAssignee.getEmail(), saved);
        }
        if (newAssignee.getEmail() != null) {
            emailService.sendBugAssignedEmail(newAssignee.getEmail(), saved);
        }

        return bugMapper.toResponse(saved);
    }

    @Override
    public BugResponse updateStatus(Integer bugId, BugStatusRequest request) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with ID: " + bugId));

        bugMapper.updateStatus(request, bug);
        if (request.getStatus() == BugStatus.CLOSED || request.getStatus() == BugStatus.RESOLVED) {
            bug.setResolvedAt(Instant.now());
        }
        bug.setUpdatedBy(authService.getCurrentUser());
        Bug saved = bugRepository.save(bug);
        return bugMapper.toResponse(saved);
    }

    @Override
    public BugResponse updateDeveloperStatus(Integer bugId, DeveloperBugStatusRequest request) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with ID: " + bugId));

        bugMapper.updateDeveloperStatus(request, bug);
        bug.setUpdatedBy(authService.getCurrentUser());
        Bug saved = bugRepository.save(bug);
        return bugMapper.toResponse(saved);
    }

    @Override
    public void deleteBug(Integer bugId) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with ID: " + bugId));

        bug.setDeleted(true);
        bug.setActive(false);
        bug.setUpdatedBy(authService.getCurrentUser());
        bugRepository.save(bug);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BugResponse> globalSearch(
            String keyword,
            BugSeverity severity,
            BugPriority priority,
            BugStatus status,
            BugCategory category,
            Integer bugOccurrence,
            Boolean isActive,
            LocalDate resolvedFrom,
            LocalDate resolvedTo,
            String timeZone,
            Pageable pageable,
            String executedBy,
            String assignedTo,
            String updatedBy
    ) {
        if (resolvedFrom != null && resolvedTo != null && resolvedFrom.isAfter(resolvedTo)) {
            throw new IllegalArgumentException("Resolved from date cannot be after resolved to date");
        }

        ZoneId zoneId = ZoneOffset.UTC;
        if (timeZone != null && !timeZone.isBlank()) {
            try {
                zoneId = ZoneId.of(timeZone);
            } catch (DateTimeException exception) {
                throw new IllegalArgumentException("Invalid timezone: " + timeZone);
            }
        }

        Instant resolvedFromInstant = null;
        Instant resolvedToInstant = null;

        if (resolvedFrom != null) {
            resolvedFromInstant = resolvedFrom.atStartOfDay(zoneId).toInstant();
        }

        if (resolvedTo != null) {
            resolvedToInstant = resolvedTo.plusDays(1).atStartOfDay(zoneId).toInstant();
        }

        Specification<Bug> specification = BugSpecification.search(
                keyword,
                severity,
                priority,
                status,
                category,
                bugOccurrence,
                isActive,
                resolvedFromInstant,
                resolvedToInstant,
                executedBy,
                assignedTo,
                updatedBy
        );

        Page<Bug> bugs = bugRepository.findAll(specification, pageable);
        return bugs.map(bugMapper::toResponse);
    }
}
