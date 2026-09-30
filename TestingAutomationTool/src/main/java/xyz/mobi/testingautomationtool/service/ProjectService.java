package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.ProjectPatchRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.ProjectPutRequest;
import xyz.mobi.testingautomationtool.dto.response.DeleteMethodDto.PatchProjectDeleteResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.PatchProjectResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.ProjectPutResponse;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;

public interface ProjectService {

    ProjectPutResponse updateProject(Integer id, ProjectPutRequest request);

    String patchProject(Integer id, ProjectPatchRequest request);

    PatchProjectResponse getProjectStatus(ProjectStatus status, Integer id);

    PatchProjectDeleteResponse softDeleteProject(Integer id);

    String hardDeleteProject(Integer id);


}
