package xyz.mobi.testingautomationtool.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.ProjectDto.*;
import xyz.mobi.testingautomationtool.entity.Attachment;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentResponse;

import java.io.IOException;
import java.util.List;

public interface ProjectService {

    ProjectResponse createProject(ProjectRequest request);

    AttachmentResponse uploadAttachment(MultipartFile file, Integer projectId) throws IOException;

    ProjectPutResponse updateProject(Integer id, ProjectPutRequest request);

    String patchProject(Integer id, ProjectPatchRequest request);

    PatchProjectDeleteResponse softDeleteProject(Integer id);

    String hardDeleteProject(Integer id);

    @Transactional
    String patchProjectActiveStatus(Integer id, ProjectStatus isActive);

    List<ProjectResponse> getAllProjects();

    ProjectResponse getProjectById(Integer projectId);

    List<ProjectResponse> searchProjects(String keyword, ProjectStatus status);

    byte[] downloadFiles(Integer projectId);
}
