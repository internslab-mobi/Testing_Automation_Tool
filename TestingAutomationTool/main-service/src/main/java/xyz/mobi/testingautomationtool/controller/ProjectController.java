package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.ApiResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.ProjectDTO.*;
import xyz.mobi.testingautomationtool.entity.Region;
import xyz.mobi.testingautomationtool.entity.SubModule;
import xyz.mobi.testingautomationtool.entity.Module;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;
import xyz.mobi.testingautomationtool.service.AttachmentService;
import xyz.mobi.testingautomationtool.service.ProjectService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;
    private final AttachmentService attachmentService;

    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", response));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
    @PostMapping(value = "/attachment/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> uploadAttachment(
            @RequestParam("file") List<MultipartFile> file,
            @PathVariable("projectId") Integer projectId
    ) throws IOException {
        List<AttachmentResponse> projectResponse = attachmentService.uploadAttachments(AttachmentType.PROJECT, projectId, file);
        return ResponseEntity.ok(ApiResponse.success("Attachments uploaded successfully", projectResponse));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects() {
        return ResponseEntity.ok(ApiResponse.success("Projects retrieved successfully", projectService.getAllProjects()));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
    @GetMapping("/{projectId:\\d+}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable("projectId") Integer projectId) {
        return ResponseEntity.ok(ApiResponse.success("Project retrieved successfully", projectService.getProjectById(projectId)));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
    @GetMapping("/{projectId:\\d+}/export")
    public ResponseEntity<byte[]> exportProjectSummary(@PathVariable("projectId") Integer projectId) {
        byte[] excelBytes = projectService.exportProjectSummary(projectId);
        String filename = "QA_Summary_Report_Project_" + projectId + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> searchProjects(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProjectStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Projects search results", projectService.searchProjects(keyword, status)));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ProjectPutRequest request) {
        ProjectResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", response));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> patchProject(
            @PathVariable("id") Integer id,
            @RequestBody ProjectPatchRequest request) {
        String response = projectService.patchProject(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasAnyRole('MANAGER')")
    @PatchMapping("/access/{id}")
    public ResponseEntity<ApiResponse<String>> patchProjectForManger(
            @PathVariable("id") Integer id,
            @RequestParam ProjectStatus status) {
        String response = projectService.patchProjectActiveStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasAnyRole('MANAGER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<PatchProjectDeleteResponse>> softDeleteProject(
            @PathVariable("id") Integer id) {
        PatchProjectDeleteResponse response = projectService.softDeleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project soft deleted successfully", response));
    }

    @PreAuthorize("hasAnyRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> hardDeleteProject(
            @PathVariable("id") Integer id) {
        String response = projectService.hardDeleteProject(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

//    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
//    @GetMapping("/projects/{projectId}/attachments/download")
//    public ResponseEntity<byte[]> downloadProjectAttachments(
//            @PathVariable Integer projectId) {
//
//        AttachmentDownloadResponse response = projectService.downloadFiles(projectId);
//
//        return ResponseEntity.ok()
//                .header(
//                        HttpHeaders.CONTENT_DISPOSITION,
//                        ContentDisposition.attachment()
//                                .filename(response.getFileName())
//                                .build()
//                                .toString()
//                )
//                .contentType(
//                        MediaType.parseMediaType(response.getContentType())
//                )
//                .contentLength(response.getFile().length)
//                .body(response.getFile());
//    }


    @PostMapping(
            value = "/{projectId}/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<List<AttachmentResponse>> uploadProjectAttachments(
            @PathVariable Integer projectId,
            @RequestParam("files") List<MultipartFile> files) {

        return ResponseEntity.ok(
                attachmentService.uploadProjectAttachments(projectId, files)
        );
    }

    @GetMapping("/{projectId:\\d+}/attachments")
    public ResponseEntity<List<AttachmentResponse>> getProjectAttachments(
            @PathVariable("projectId") Integer projectId) {

        return ResponseEntity.ok(
                attachmentService.getAttachments(
                        AttachmentType.PROJECT,
                        projectId
                )
        );
    }

    @GetMapping("/{projectId:\\d+}/attachments/download")
    public ResponseEntity<ByteArrayResource> downloadProjectAttachments(
            @PathVariable("projectId") Integer projectId) {

        AttachmentDownloadResponse response =
                attachmentService.downloadAttachments(
                        AttachmentType.PROJECT,
                        projectId
                );

        ByteArrayResource resource =
                new ByteArrayResource(response.getFile());

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(response.getContentType())
                )
                .contentLength(response.getFile().length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(response.getFileName())
                                .build()
                                .toString()
                )
                .body(resource);
    }


    @PostMapping("/module")
    public ResponseEntity<Module> createModule(
            @RequestBody Map<String, String> request) {

        Module module = projectService.createModule(
                request.get("moduleName")
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(module);
    }

    @PostMapping("/{moduleId}/sub-modules")
    public ResponseEntity<SubModule> createSubModule(
            @PathVariable Integer moduleId,
            @RequestBody Map<String, String> request) {

        SubModule subModule = projectService.createSubModule(
                moduleId,
                request.get("subModuleName")
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(subModule);
    }

    @PostMapping("/region")
    public ResponseEntity<Region> createRegion(
            @RequestBody Map<String, String> request) {

        Region region = projectService.createRegion(
                request.get("regionName")
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(region);
    }
}
