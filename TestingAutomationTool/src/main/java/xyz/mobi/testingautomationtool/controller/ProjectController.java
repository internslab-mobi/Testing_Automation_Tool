package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.ProjectRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.ProjectResponse;
import xyz.mobi.testingautomationtool.service.ProjectService;

import java.io.IOException;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request) {

        ProjectResponse response =
                projectService.createProject(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping(
            value = "/{projectId}/document",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<AttachmentResponse> uploadProjectDocument(
            @PathVariable Integer projectId,
            @RequestParam("file") MultipartFile file)
            throws IOException {

        return ResponseEntity.ok(
                projectService.uploadProjectDocument(file, projectId)
        );
    }
}