package xyz.mobi.testingautomationtool.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    BugResponse patchBug(Integer bugId, BugPatchRequest request);

    BugResponse assignBug(Integer bugId, BugAssignRequest request);

    BugResponse updateStatus(Integer bugId, BugStatusRequest request);

    BugResponse updateDeveloperStatus(Integer bugId, DeveloperBugStatusRequest request);

    void deleteBug(Integer bugId);

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
}