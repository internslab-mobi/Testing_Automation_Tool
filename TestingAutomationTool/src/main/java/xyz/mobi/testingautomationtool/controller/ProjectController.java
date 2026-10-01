package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.ProjectDto.*;
import xyz.mobi.testingautomationtool.entity.Attachment;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;
import xyz.mobi.testingautomationtool.service.AttachmentService;
import xyz.mobi.testingautomationtool.service.ProjectService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/attachment/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentResponse> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @PathVariable("projectId") Integer projectId
    ) throws IOException {
        xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentResponse projectResponse = projectService.uploadAttachment(file, projectId);
        return ResponseEntity.ok(projectResponse);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Integer projectId) {
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProjectResponse>> searchProjects(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProjectStatus status) {
        return ResponseEntity.ok(projectService.searchProjects(keyword, status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectPutResponse> updateProject(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ProjectPutRequest request) {
        ProjectPutResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> patchProject(
            @PathVariable("id") Integer id,
            @RequestBody ProjectPatchRequest request) {
        String response = projectService.patchProject(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<PatchProjectDeleteResponse> softDeleteProject(
            @PathVariable("id") Integer id) {
        PatchProjectDeleteResponse response = projectService.softDeleteProject(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> hardDeleteProject(
            @PathVariable("id") Integer id) {
        String response = projectService.hardDeleteProject(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{projectId}/attachments/download")
    public ResponseEntity<byte[]> downloadProjectAttachments(
            @PathVariable Integer projectId) {

        byte[] zipFile = projectService.downloadFiles(projectId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"project_" + projectId + "_attachments.zip\""
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(zipFile.length)
                .body(zipFile);
    }
}
