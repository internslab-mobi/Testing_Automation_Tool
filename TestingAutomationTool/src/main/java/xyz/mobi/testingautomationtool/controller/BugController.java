package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugAssignRequest;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugPatchRequest;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugStatusRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.BugPutRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse;
import xyz.mobi.testingautomationtool.service.BugService;

@RestController
@RequestMapping("/bugs")
@RequiredArgsConstructor
public class BugController {

    private final BugService bugService;

    @PutMapping("/{bugId}")
    public ResponseEntity<BugResponse> updateBug(
            @PathVariable Integer bugId,
            @Valid @RequestBody BugPutRequest request) {

        return ResponseEntity.ok(
                bugService.updateBug(bugId, request));
    }

    @PatchMapping("/{bugId}")
    public ResponseEntity<BugResponse> patchBug(
            @PathVariable Integer bugId,
            @Valid @RequestBody BugPatchRequest request) {

        return ResponseEntity.ok(
                bugService.patchBug(bugId, request)
        );
    }

    @PatchMapping("/{bugId}/delete")
    public ResponseEntity<Void> softDeleteBug(
            @PathVariable Integer bugId) {

        bugService.softDeleteBug(bugId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{bugId}")
    public ResponseEntity<Void> hardDeleteBug(
            @PathVariable Integer bugId) {

        bugService.hardDeleteBug(bugId);

        return ResponseEntity.noContent().build();
    }



}
