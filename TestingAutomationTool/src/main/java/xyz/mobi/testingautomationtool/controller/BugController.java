package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugAssignRequest;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugStatusRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.BugPutRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse;
import xyz.mobi.testingautomationtool.service.BugService;

import java.util.List;

@RestController
@RequestMapping("/bugs")
@RequiredArgsConstructor
public class BugController {

    private final BugService bugService;


    @PatchMapping("/{bugId}/delete")
    public ResponseEntity<String> softDeleteBug(
            @PathVariable Integer bugId) {

        return ResponseEntity.ok(
                bugService.softDeleteBug(bugId));
    }


    @DeleteMapping("/{bugId}")
    public ResponseEntity<String> hardDeleteBug(
            @PathVariable Integer bugId) {

        return ResponseEntity.ok(
                bugService.hardDeleteBug(bugId));
    }


    @PutMapping("/{bugId}")
    public ResponseEntity<BugResponse> updateBug(
            @PathVariable Integer bugId,
            @Valid @RequestBody BugPutRequest request) {

        return ResponseEntity.ok(
                bugService.updateBug(bugId, request));
    }


    @PatchMapping("/{bugId}/assign")
    public ResponseEntity<BugResponse> assignBug(
            @PathVariable Integer bugId,
            @Valid @RequestBody BugAssignRequest request) {

        return ResponseEntity.ok(
                bugService.assignBug(bugId, request));
    }


    @PatchMapping("/{bugId}/status")
    public ResponseEntity<BugResponse> updateStatus(
            @PathVariable Integer bugId,
            @Valid @RequestBody BugStatusRequest request) {

        return ResponseEntity.ok(
                bugService.updateStatus(bugId, request));
    }


    @GetMapping("/{bugId}")
    public ResponseEntity<BugResponse> getById(
            @PathVariable Integer bugId) {

        return ResponseEntity.ok(
                bugService.getById(bugId));
    }


    @GetMapping
    public ResponseEntity<List<BugResponse>> getByAll() {

        return ResponseEntity.ok(
                bugService.getByAll());
    }


    @GetMapping("/testcase/{testcaseId}")
    public ResponseEntity<List<BugResponse>> getByTestcaseId(
            @PathVariable Integer testcaseId) {

        return ResponseEntity.ok(
                bugService.getByTestcaseId(testcaseId));
    }

    @GetMapping("/feature/{featureId}")
    public ResponseEntity<List<BugResponse>> getByFeatureId(
            @PathVariable Integer featureId) {

        return ResponseEntity.ok(
                bugService.getByFeatureId(featureId));
    }

    @PostMapping("/{bugId}/reoccurrence")
    public ResponseEntity<BugResponse> createReoccurrence(
            @PathVariable Integer bugId) {

        return ResponseEntity.ok(
                bugService.createReoccurrence(bugId));
    }
}
