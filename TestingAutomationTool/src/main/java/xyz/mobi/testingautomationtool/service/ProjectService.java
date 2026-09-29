package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.ProjectPatchRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.ProjectPutRequest;
import xyz.mobi.testingautomationtool.dto.response.DeleteMethodDto.PatchProjectDeleteResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.PatchProjectResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.ProjectPutResponse;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;
import xyz.mobi.testingautomationtool.dto.ProjectDTO.ProjectResponse;

import java.util.List;

public interface ProjectService {

    ProjectPutResponse updateProject(Integer id, ProjectPutRequest request);

    String patchProject(Integer id, ProjectPatchRequest request);

    PatchProjectResponse getProjectStatus(ProjectStatus status, Integer id);

    PatchProjectDeleteResponse softDeleteProject(Integer id);

    String hardDeleteProject(Integer id);


    List<ProjectResponse> getAllProjects();
    ProjectResponse getProjectById(Integer projectId);
}
