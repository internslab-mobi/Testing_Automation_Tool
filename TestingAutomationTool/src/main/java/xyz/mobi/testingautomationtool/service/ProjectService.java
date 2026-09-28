package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.ProjectPatchRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.ProjectPutRequest;
import xyz.mobi.testingautomationtool.dto.response.DeleteMethodDto.PatchProjectDeleteResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.PatchProjectResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.ProjectPutResponse;

public interface ProjectService {

    ProjectPutResponse updateProject(Integer id, ProjectPutRequest request);

    PatchProjectResponse patchProject(Integer id, ProjectPatchRequest request);

    PatchProjectDeleteResponse softDeleteProject(Integer id);

    String hardDeleteProject(Integer id);
}
