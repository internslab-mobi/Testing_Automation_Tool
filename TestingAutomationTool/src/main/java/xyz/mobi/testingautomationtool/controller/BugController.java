package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugAssignRequest;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugStatusRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.BugPutRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;
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
    public ResponseEntity<Page<BugResponse>> getAllBugs(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        return ResponseEntity.ok(
                bugService.getAllBugs(page, size));
    }

    @GetMapping("/all")
    public ResponseEntity<List<BugResponse>> getByAll() {

        return ResponseEntity.ok(
                bugService.getByAll());
    }

    @GetMapping("/testcase/{testcaseId}")
    public ResponseEntity<Page<BugResponse>> getByTestcaseId(
            @PathVariable Integer testcaseId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        return ResponseEntity.ok(
                bugService.getByTestcaseId(testcaseId, page, size));
    }

    @GetMapping("/feature/{featureId}")
    public ResponseEntity<Page<BugResponse>> getByFeatureId(
            @PathVariable Integer featureId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        return ResponseEntity.ok(
                bugService.getByFeatureId(featureId, page, size));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<BugResponse>> getByStatus(
            @PathVariable BugStatus status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        return ResponseEntity.ok(
                bugService.getByStatus(status, page, size));
    }

    @GetMapping("/assigned/{userId}")
    public ResponseEntity<Page<BugResponse>> getByAssignedTo(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        return ResponseEntity.ok(
                bugService.getByAssignedTo(userId, page, size));
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<Page<BugResponse>> getBySeverity(
            @PathVariable BugSeverity severity,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        return ResponseEntity.ok(
                bugService.getBySeverity(severity, page, size));
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<Page<BugResponse>> getByPriority(
            @PathVariable BugPriority priority,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        return ResponseEntity.ok(
                bugService.getByPriority(priority, page, size));
    }

    @GetMapping("/tester/{testerId}/bugs")
    public ResponseEntity<Page<BugResponse>> getByAssignedBYTester(
            @PathVariable(name = "testerId") Integer testerId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        return ResponseEntity.ok(
                bugService.getByReportedBy(testerId, page, size));
    }
}
