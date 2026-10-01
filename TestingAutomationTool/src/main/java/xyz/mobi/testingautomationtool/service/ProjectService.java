package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.ProjectDto.*;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;

import java.util.List;

public interface ProjectService {

    ProjectResponse createProject(ProjectRequest request);

    ProjectPutResponse updateProject(Integer id, ProjectPutRequest request);

    String patchProject(Integer id, ProjectPatchRequest request);

    PatchProjectDeleteResponse softDeleteProject(Integer id);

    String hardDeleteProject(Integer id);

    List<ProjectResponse> getAllProjects();

    ProjectResponse getProjectById(Integer projectId);

    List<ProjectResponse> searchProjects(String keyword, ProjectStatus status);
}
