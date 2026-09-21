package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugAssignRequest;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugStatusRequest;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.BugRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.BugPutRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse;

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

    List<BugResponse> getByTestcaseId(
            Integer testcaseId);

    List<BugResponse> getByFeatureId(Integer featureId);

    BugResponse createReoccurrence(
            Integer bugId);
}