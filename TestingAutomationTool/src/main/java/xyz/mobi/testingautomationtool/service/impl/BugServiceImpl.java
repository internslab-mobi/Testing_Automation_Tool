package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.BugDTO.BugRequest;
import xyz.mobi.testingautomationtool.dto.BugDTO.BugResponse;
import xyz.mobi.testingautomationtool.entity.*;

import xyz.mobi.testingautomationtool.entity.Bug;

import xyz.mobi.testingautomationtool.enums.*;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.repository.*;

import xyz.mobi.testingautomationtool.repository.BugRepository;

import xyz.mobi.testingautomationtool.service.BugService;
import xyz.mobi.testingautomationtool.specification.BugSpecification;

import java.time.*;


@Service
@RequiredArgsConstructor
@Transactional
public class BugServiceImpl implements BugService {

    private final BugRepository bugRepository;
    private final TestCaseRepository testCaseRepository;
    private final FeatureRepository featureRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;


    @Transactional
    @Override
    public BugResponse createBug(BugRequest request) {

        // Step 1: Duplicate guard - verify bugFormatId is unique
        if (bugRepository.existsByBugFormatId(request.getBugFormatId())) {
            throw new RuntimeException("Bug with format ID '" + request.getBugFormatId() + "' already exists");
        }

        // Step 2: Restrict bug creation status to OPEN only
        if (request.getStatus() != null && request.getStatus() != BugStatus.OPEN) {
            throw new RuntimeException("New bug can only be created with OPEN status, received: " + request.getStatus());
        }

        // Step 3: Fetch and validate test case
        TestCase testCase = testCaseRepository.findById(request.getTestCaseId())
                .orElseThrow(() -> new RuntimeException("Test case not found with id: " + request.getTestCaseId()));

        Integer featureId = request.getFeatureId() != null ? request.getFeatureId() : testCase.getFeatureId();
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new RuntimeException("Feature not found with id: " + featureId));

        // Step 4: Fetch reportedBy user
        User reportedBy = userRepository.findById(request.getReportedBy())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getReportedBy()));

        // Step 5: Fetch optional assignedTo user
        User assignedTo = null;
        if (request.getAssignedTo() != null) {
            assignedTo = userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found with id: " + request.getAssignedTo()));
        }

        // Step 6: Validate reoccurred bug relevance and compute occurrence server-side
        Bug bugReoccurred = null;
        int occurrence = 1;
        if (request.getBugReoccurredId() != null) {
            bugReoccurred = bugRepository.findById(request.getBugReoccurredId())
                    .orElseThrow(() -> new RuntimeException("Previous bug not found with id: " + request.getBugReoccurredId()));

            // Ensure the reoccurred bug belongs to the same test case
            if (bugReoccurred.getTestCase() != null &&
                    !bugReoccurred.getTestCase().getTestcaseId().equals(testCase.getTestcaseId())) {
                throw new RuntimeException("Reoccurred bug (ID: " + request.getBugReoccurredId()
                        + ") does not belong to the same test case (expected testCaseId: "
                        + testCase.getTestcaseId() + ", but was: "
                        + bugReoccurred.getTestCase().getTestcaseId() + ")");
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

    @Override
    @Transactional(readOnly = true)
    public xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse getById(Integer bugId) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Bug not found with ID: " + bugId));

        if (!bug.isActive()) {
            throw new ResourceNotFoundException("The bug has been removed with ID: " + bugId);
        }

        return bugMapper.toResponse(bug);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse> getAllBugs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bugRepository.findByIsActiveTrue(pageable)
                .map(bugMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse> globalSearch(
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

        if (resolvedFrom != null
                && resolvedTo != null
                && resolvedFrom.isAfter(resolvedTo)) {

            throw new IllegalArgumentException(
                    "Resolved from date cannot be after resolved to date"
            );
        }

        ZoneId zoneId = ZoneOffset.UTC;

        if (timeZone != null && !timeZone.isBlank()) {

            try {
                zoneId = ZoneId.of(timeZone);

            } catch (DateTimeException exception) {

                throw new IllegalArgumentException(
                        "Invalid timezone: " + timeZone
                );
            }
        }

        Instant resolvedFromInstant = null;
        Instant resolvedToInstant = null;

        if (resolvedFrom != null) {

            resolvedFromInstant = resolvedFrom
                    .atStartOfDay(zoneId)
                    .toInstant();
        }

        if (resolvedTo != null) {

            resolvedToInstant = resolvedTo
                    .plusDays(1)
                    .atStartOfDay(zoneId)
                    .toInstant();
        }

        Specification<Bug> specification =
                BugSpecification.search(
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

        Page<Bug> bugs =
                bugRepository.findAll(
                        specification,
                        pageable
                );

        return bugs.map(bugMapper::toResponse);
    }

}
