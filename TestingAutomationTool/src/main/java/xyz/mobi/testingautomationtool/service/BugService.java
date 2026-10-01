package xyz.mobi.testingautomationtool.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.BugDto.*;
import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import java.time.LocalDate;

public interface BugService {
    BugResponse createBug(BugRequest request);

    BugResponse getById(Integer bugId);

    Page<BugResponse> getAllBugs(int page, int size);

    BugResponse updateBug(Integer bugId, BugPutRequest request);

    String patchBug(Integer bugId, BugPatchRequest request);

    void deleteBug(Integer bugId);

    void hardDelete(Integer bugId);

    Page<BugResponse> globalSearch(
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
    );

    @Transactional(readOnly = true)
    AttachmentDownloadResponse downloadBugAttachments(Integer bugId);
}