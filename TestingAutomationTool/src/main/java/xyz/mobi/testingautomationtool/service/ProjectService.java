package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.ProjectDTO.ProjectResponse;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;

import java.util.List;

public interface ProjectService {
    List<ProjectResponse> getAllProjects();
    ProjectResponse getProjectById(Integer projectId);
    List<ProjectResponse> searchProjects(String keyword, ProjectStatus status);
}
