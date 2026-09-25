package xyz.mobi.testingautomationtool.service;

import org.springframework.data.domain.Page;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugAssignRequest;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugStatusRequest;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.BugRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.BugPutRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import java.util.List;

public interface BugService {

    BugResponse createBug(BugRequest request, Integer testCaseId);

    String softDeleteBug(Integer bugId);

    String hardDeleteBug(Integer bugId);

    BugResponse updateBug(
            Integer bugId,
            BugPutRequest request);

    BugResponse assignBug(
            Integer bugId,
            BugAssignRequest request);

    BugResponse updateStatus(
            Integer bugId,
            BugStatusRequest request);

    BugResponse getById(Integer bugId);

    List<BugResponse> getByAll();

    Page<BugResponse> getAllBugs(int page, int size);

//    List<BugResponse> getByTestcaseId(Integer testcaseId);

    Page<BugResponse> getByTestcaseId(Integer testcaseId, int page, int size);

//    List<BugResponse> getByFeatureId(Integer featureId);

    Page<BugResponse> getByFeatureId(Integer featureId, int page, int size);

    Page<BugResponse> getByStatus(BugStatus status, int page, int size);

    Page<BugResponse> getByAssignedTo(Integer userId, int page, int size);

    Page<BugResponse> getBySeverity(BugSeverity severity, int page, int size);

    Page<BugResponse> getByPriority(BugPriority priority, int page, int size);

    Page<BugResponse> getByReportedBy(Integer id,int page,int size);
}