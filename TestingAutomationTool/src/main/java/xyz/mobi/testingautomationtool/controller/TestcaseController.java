package xyz.mobi.testingautomationtool.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.TestcaseDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;
import xyz.mobi.testingautomationtool.service.TestCaseService;

@RestController
@RequestMapping("/api/v1/test-cases")
@RequiredArgsConstructor
public class TestcaseController {

    private final TestCaseService testCaseService;

    @GetMapping({"", "/feature/{featureId}"})
    public ResponseEntity<Page<TestCaseResponse>> getAllTestCases(
            @PathVariable(name = "featureId", required = false) Integer featureId,
            @RequestParam(required = false) TestCaseStatus status,
            @RequestParam(required = false) TestType type,
            @RequestParam(required = false) TestPriority priority,
            @PageableDefault(page = 0, size = 10, sort = "testcaseId") Pageable pageable) {
        if (pageable.getPageSize() <= 0) {
            pageable = PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
        }
        return ResponseEntity.ok(
                testCaseService.getAll(featureId, status, type, priority, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestCaseResponse> getTestCaseById(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(testCaseService.getById(id, includeInactive));
    }
}
