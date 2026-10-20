package xyz.mobi.testingautomationtool.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.ProjectDTO.*;
import xyz.mobi.testingautomationtool.entity.Module;
import xyz.mobi.testingautomationtool.entity.Region;
import xyz.mobi.testingautomationtool.entity.SubModule;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentResponse;

import java.io.IOException;
import java.util.List;

public interface ProjectService {

    ProjectResponse createProject(ProjectRequest request);

//    AttachmentResponse uploadAttachment(MultipartFile file, Integer projectId) throws IOException;

    ProjectResponse updateProject(Integer id, ProjectPutRequest request);

    String patchProject(Integer id, ProjectPatchRequest request);

    PatchProjectDeleteResponse softDeleteProject(Integer id);

    String hardDeleteProject(Integer id);

    String patchProjectActiveStatus(Integer id, ProjectStatus isActive);

    List<ProjectResponse> getAllProjects();

    ProjectResponse getProjectById(Integer projectId);

    List<ProjectResponse> searchProjects(String keyword, ProjectStatus status);

    byte[] exportProjectSummary(Integer projectId);

    @Transactional
    Module createModule(String moduleName);

    List<Module> getAllModules();

    @Transactional
    SubModule createSubModule(
            Integer moduleId,
            String subModuleName);

    List<SubModule> getSubModulesByModuleId(Integer moduleId);

    @Transactional
    Region createRegion(String regionName);

    List<Region> getAllRegions();
}
