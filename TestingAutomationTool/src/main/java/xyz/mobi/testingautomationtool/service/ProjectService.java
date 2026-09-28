package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.ProjectRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.ProjectResponse;

public interface ProjectService {

    ProjectResponse createProject(ProjectRequest request);
}
