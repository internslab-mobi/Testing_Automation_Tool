package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.ProjectDTO.*;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;
import xyz.mobi.testingautomationtool.service.AttachmentService;
import xyz.mobi.testingautomationtool.service.ProjectService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")

public class ProjectController {

    private final ProjectService projectService;
    private final AttachmentService attachmentService;

    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
    @PostMapping(value = "/attachment/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<AttachmentResponse>> uploadAttachment(
            @RequestParam("file") List<MultipartFile> file,
            @PathVariable("projectId") Integer projectId
    ) throws IOException {
        List<AttachmentResponse> projectResponse = attachmentService.uploadAttachments(AttachmentType.PROJECT,projectId,file);
        return ResponseEntity.ok(projectResponse);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Integer projectId) {
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<ProjectResponse>> searchProjects(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProjectStatus status) {
        return ResponseEntity.ok(projectService.searchProjects(keyword, status));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectPutResponse> updateProject(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ProjectPutRequest request) {
        ProjectPutResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<String> patchProject(
            @PathVariable("id") Integer id,
            @RequestBody ProjectPatchRequest request) {
        String response = projectService.patchProject(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('MANAGER')")
    @PatchMapping("/access/{id}")
    public ResponseEntity<String> patchProjectForManger(@PathVariable("id") Integer id,
                                                        @RequestParam ProjectStatus status                                          ){
        String response = projectService.patchProjectActiveStatus(id,status);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('MANAGER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<PatchProjectDeleteResponse> softDeleteProject(
            @PathVariable("id") Integer id) {
        PatchProjectDeleteResponse response = projectService.softDeleteProject(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @PreAuthorize("hasAnyRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> hardDeleteProject(
            @PathVariable("id") Integer id) {
        String response = projectService.hardDeleteProject(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
    @GetMapping("/projects/{projectId}/attachments/download")
    public ResponseEntity<byte[]> downloadProjectAttachments(
            @PathVariable Integer projectId) {

        AttachmentDownloadResponse response =
                projectService.downloadFiles(projectId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(response.getFileName())
                                .build()
                                .toString()
                )
                .contentType(
                        MediaType.parseMediaType(response.getContentType())
                )
                .contentLength(response.getFile().length)
                .body(response.getFile());
    }
}
