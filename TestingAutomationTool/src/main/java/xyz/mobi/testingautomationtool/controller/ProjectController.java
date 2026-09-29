package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.ProjectPatchRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.ProjectPutRequest;
import xyz.mobi.testingautomationtool.dto.response.DeleteMethodDto.PatchProjectDeleteResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.PatchProjectResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.ProjectPutResponse;
import xyz.mobi.testingautomationtool.service.ProjectService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping({"/project"})
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
public class ProjectController {

    private final ProjectService projectService;

    @PutMapping("/{id}")
    public ResponseEntity<ProjectPutResponse> updateProject(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ProjectPutRequest request) {

        ProjectPutResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PatchProjectResponse> patchProject(
            @PathVariable("id") Integer id,
            @RequestBody ProjectPatchRequest request) {

        PatchProjectResponse response = projectService.patchProject(id, request);
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
}
