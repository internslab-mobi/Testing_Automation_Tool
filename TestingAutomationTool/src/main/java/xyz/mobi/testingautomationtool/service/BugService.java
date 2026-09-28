package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugPatchRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.BugPutRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse;

public interface BugService {

    BugResponse updateBug(Integer bugId, BugPutRequest request);

    BugResponse patchBug(Integer bugId, BugPatchRequest request);

    void softDeleteBug(Integer bugId);

    void hardDeleteBug(Integer bugId);

}