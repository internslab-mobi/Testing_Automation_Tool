package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.ProjectDTO.ProjectResponse;

import java.util.List;

public interface ProjectService {
    List<ProjectResponse> getAllProjects();
    ProjectResponse getProjectById(Integer projectId);
}
