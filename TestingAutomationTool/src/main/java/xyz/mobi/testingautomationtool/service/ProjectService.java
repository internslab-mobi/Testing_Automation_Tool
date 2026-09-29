package xyz.mobi.testingautomationtool.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.ProjectRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.ProjectResponse;

import java.io.IOException;

public interface ProjectService {

    ProjectResponse createProject(ProjectRequest request);

    @Transactional
    AttachmentResponse uploadProjectDocument(
            MultipartFile file,
            Integer projectId) throws IOException;
}
