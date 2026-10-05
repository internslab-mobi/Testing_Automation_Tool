package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.BugDto.*;
import xyz.mobi.testingautomationtool.dto.BugDto.GetBugResponse.BugResponse;
import xyz.mobi.testingautomationtool.dto.NotificationDto.NotificationRequest;
import xyz.mobi.testingautomationtool.entity.*;
import xyz.mobi.testingautomationtool.enums.*;
import xyz.mobi.testingautomationtool.exception.AttachmentProcessingException;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.BugMapper;
import xyz.mobi.testingautomationtool.repository.*;
import xyz.mobi.testingautomationtool.service.AuthService;
import xyz.mobi.testingautomationtool.service.BugService;
import xyz.mobi.testingautomationtool.service.NotificationService;
import xyz.mobi.testingautomationtool.specification.BugSpecification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    private final BugMapper bugMapper;
    private final AuthService authService;
    private final AttachmentRepository attachmentRepository;

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
            notificationService.createNotification(
                    NotificationRequest.builder()
                            .employeeId(reporter.getUserId())
                            .assignedId(assignedTo.getUserId())
                            .bugId(savedBug.getBugId())
                            .build()
            );
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
    @Transactional
    public BugResponse updateBug(Integer bugId, BugPutRequest request) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bug not found with ID: " + bugId));

        if (!bug.isActive()) {
            throw new IllegalStateException(
                    "Cannot update disabled bug with ID: " + bugId);
        }

        if (bug.isDeleted()) {
            throw new IllegalStateException(
                    "Cannot update deleted bug with ID: " + bugId);
        }

        User currentUser = authService.getCurrentUser();

        Integer oldAssignedUserId =
                bug.getAssignedTo() != null
                        ? bug.getAssignedTo().getUserId()
                        : null;

        BugStatus oldStatus = bug.getStatus();

        bugMapper.updateEntity(bug, request);

        if (request.getStatus() != null) {
            bug.setStatus(request.getStatus());
        }

        if (request.getAssignedTo() != null) {

            User assignedTo = userRepository.findById(
                    request.getAssignedTo()
            ).orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Assigned user not found with ID: "
                                    + request.getAssignedTo()));

            bug.setAssignedTo(assignedTo);

            if (request.getStatus() == null) {
                bug.setStatus(BugStatus.IN_PROGRESS);
            }
        }

        if (!Objects.equals(oldStatus, bug.getStatus())) {
            bug.setExecutedBy(currentUser);
        }

        bug.setUpdatedBy(currentUser);

        if (bug.getStatus() == BugStatus.RESOLVED) {

            if (bug.getResolvedAt() == null) {
                bug.setResolvedAt(Instant.now());
            }

        } else {
            bug.setResolvedAt(null);
        }

        boolean statusChanged =
                !Objects.equals(
                        oldStatus,
                        bug.getStatus()
                );

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

        if (statusChanged || assignmentChanged) {

            BugHistory history = BugHistory.builder()
                    .bug(bug)
                    .executedBy(currentUser.getUserId())
                    .bugStatus(bug.getStatus())
                    .assignedTo(newAssignedUserId)
                    .createdAt(Instant.now())
                    .build();

            bugHistoryRepository.save(history);
        }

        if (assignmentChanged && newAssignedUserId != null) {

            if (oldAssignedUserId == null) {

                notificationService.createNotification(
                        NotificationRequest.builder()
                                .employeeId(currentUser.getUserId())
                                .assignedId(newAssignedUserId)
                                .bugId(bug.getBugId())
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

        return bugMapper.toResponse(bug);
    }

    @Override
    @Transactional
    public String patchBug(Integer bugId, BugPatchRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Bug patch request cannot be null");
        }

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bug not found with ID: " + bugId));

        if (!bug.isActive()) {
            throw new IllegalStateException(
                    "Cannot update disabled bug with ID: " + bugId);
        }

        if (bug.isDeleted()) {
            throw new ResourceNotFoundException(
                    "Bug is deleted with ID: " + bugId);
        }

        User currentUser = authService.getCurrentUser();

        List<String> updatedFields = new ArrayList<>();

        BugStatus oldStatus = bug.getStatus();

        Integer oldAssignedUserId =
                bug.getAssignedTo() != null
                        ? bug.getAssignedTo().getUserId()
                        : null;

        if (request.getTitle() != null
                && !request.getTitle().isBlank()) {

            bug.setTitle(request.getTitle());
            updatedFields.add("title");
        }

        if (request.getDescription() != null
                && !request.getDescription().isBlank()) {

            bug.setDescription(request.getDescription());
            updatedFields.add("description");
        }

        if (request.getSeverity() != null) {
            bug.setSeverity(request.getSeverity());
            updatedFields.add("severity");
        }

        if (request.getPriority() != null) {
            bug.setPriority(request.getPriority());
            updatedFields.add("priority");
        }

        if (request.getCategory() != null) {
            bug.setCategory(request.getCategory());
            updatedFields.add("category");
        }

        if (request.getComments() != null) {
            bug.setComments(request.getComments());
            updatedFields.add("comments");
        }

        if (request.getDynamicFields() != null) {

            if (bug.getDynamicFields() == null) {
                bug.setDynamicFields(new HashMap<>());
            }

            Bug finalBug = bug;
            request.getDynamicFields().forEach((key, value) -> {

                if (value == null) {
                    finalBug.getDynamicFields().remove(key);
                } else {
                    finalBug.getDynamicFields().put(key, value);
                }
            });

            updatedFields.add("dynamicFields");
        }

        if (request.getStatus() != null) {

            bug.setStatus(request.getStatus());
            updatedFields.add("status");
        }

        if (request.getAssignedTo() != null) {

            User newAssignee = userRepository.findById(
                    request.getAssignedTo()
            ).orElseThrow(() ->
                    new ResourceNotFoundException(
                            "User not found with ID: "
                                    + request.getAssignedTo()));

            bug.setAssignedTo(newAssignee);

            updatedFields.add("assignedTo");
        }

        boolean statusChanged =
                !Objects.equals(
                        oldStatus,
                        bug.getStatus()
                );

        Integer newAssignedUserId =
                bug.getAssignedTo() != null
                        ? bug.getAssignedTo().getUserId()
                        : null;

        boolean assignmentChanged =
                !Objects.equals(
                        oldAssignedUserId,
                        newAssignedUserId
                );


        if (assignmentChanged
                && request.getStatus() == null) {

            bug.setStatus(BugStatus.IN_PROGRESS);

            if (!statusChanged) {
                updatedFields.add("status");
            }
        }

        // Recalculate status change after assignment logic
        statusChanged =
                !Objects.equals(
                        oldStatus,
                        bug.getStatus()
                );

        if (statusChanged) {
            bug.setExecutedBy(currentUser);
        }

        if (bug.getStatus() == BugStatus.RESOLVED) {

            if (bug.getResolvedAt() == null) {
                bug.setResolvedAt(Instant.now());
            }

        } else {
            bug.setResolvedAt(null);
        }

        if (updatedFields.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one field must be provided for update");
        }

        bug.setUpdatedBy(currentUser);


        bug = bugRepository.save(bug);

        if (statusChanged || assignmentChanged) {

            BugHistory history = BugHistory.builder()
                    .bug(bug)
                    .executedBy(currentUser.getUserId())
                    .bugStatus(bug.getStatus())
                    .assignedTo(newAssignedUserId)
                    .createdAt(Instant.now())
                    .build();

            bugHistoryRepository.save(history);
        }


        if (assignmentChanged
                && newAssignedUserId != null) {

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

        return "Bug with ID " + bugId
                + " updated successfully. Changed fields: "
                + String.join(", ", updatedFields);
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
    public void hardDelete(Integer bugId) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with ID: " + bugId));
        bugRepository.delete(bug);
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


    @Transactional(readOnly = true)
    @Override
    public AttachmentDownloadResponse downloadBugAttachments(Integer bugId) {

        List<Attachment> attachments =
                attachmentRepository
                        .findAllByBug_BugIdAndIsDeletedFalseAndIsActiveTrue(bugId);

        if (attachments.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No attachments found for bug ID: " + bugId
            );
        }

        List<Attachment> validAttachments = attachments.stream()
                .filter(attachment ->
                        attachment.getFileName() != null &&
                                !attachment.getFileName().isBlank() &&
                                attachment.getFileBlob() != null
                )
                .toList();

        if (validAttachments.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No valid attachments found for bug ID: " + bugId
            );
        }

        // One file → download original file
        if (validAttachments.size() == 1) {

            Attachment attachment = validAttachments.get(0);

            String contentType = attachment.getFileType() != null
                    ? attachment.getFileType()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;

            return new AttachmentDownloadResponse(
                    attachment.getFileBlob(),
                    attachment.getFileName(),
                    contentType
            );
        }

        // Multiple files → ZIP
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            Set<String> fileNames = new HashSet<>();

            for (Attachment attachment : validAttachments) {

                String fileName = attachment.getFileName();

                if (!fileNames.add(fileName)) {
                    continue;
                }

                ZipEntry zipEntry = new ZipEntry(fileName);

                zos.putNextEntry(zipEntry);
                zos.write(attachment.getFileBlob());
                zos.closeEntry();
            }

            zos.finish();

            return new AttachmentDownloadResponse(
                    baos.toByteArray(),
                    "bug-" + bugId + "-attachments.zip",
                    "application/zip"
            );

        } catch (IOException e) {

            throw new AttachmentProcessingException(
                    "Failed to create ZIP file for bug ID: " + bugId
            );
        }
    }
    @Transactional
    public List<AttachmentResponse> uploadAttachments(
            List<MultipartFile> files,
            Integer bugId) throws IOException {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bug is not present for this id: " + bugId
                ));

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided");
        }

        User user = authService.getCurrentUser();

        List<Attachment> attachments = new ArrayList<>();
        Set<String> uploadedFileNames = new HashSet<>();

        // Validate all files before saving
        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException(
                        "Uploaded file cannot be empty"
                );
            }

            String filename = file.getOriginalFilename();

            if (filename == null || filename.isBlank()) {
                filename = "Bug_attachment_" + UUID.randomUUID();
            }

            // Check duplicates within this upload
            if (!uploadedFileNames.add(filename)) {
                throw new IllegalArgumentException(
                        "Duplicate filename in upload: " + filename
                );
            }

            // Check duplicates in the database
            boolean exists = attachmentRepository
                    .existsByBug_BugIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(
                            bugId, filename
                    );

            if (exists) {
                throw new IllegalArgumentException(
                        "File already exists in this bug: " + filename
                );
            }

            Attachment attachment = Attachment.builder()
                    .attachmentType(AttachmentType.PROJECT)
                    .bug(bug)
                    .fileName(filename)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileBlob(file.getBytes())
                    .uploadedBy(user)
                    .updatedBy(user)
                    .isActive(true)
                    .isDeleted(false)
                    .build();

            attachments.add(attachment);
        }

        // Save all attachments
        List<Attachment> savedAttachments =
                attachmentRepository.saveAll(attachments);

        // Build response
        return savedAttachments.stream()
                .map(attachment -> AttachmentResponse.builder()
                        .attachmentId(attachment.getAttachmentId())
                        .attachmentType(AttachmentType.BUG)
                        .bugId(bugId)
                        .fileName(attachment.getFileName())
                        .fileType(attachment.getFileType())
                        .fileSize(attachment.getFileSize())
                        .uploadedBy(user != null ? user.getUserId() : null)
                        .createdAt(attachment.getCreatedAt())
                        .build())
                .toList();
    }

}
