package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.ApiResponse;
import xyz.mobi.testingautomationtool.dto.ExcelDTO.ExcelUploadResponse;
import xyz.mobi.testingautomationtool.dto.TestCaseDTO.*;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;
import xyz.mobi.testingautomationtool.service.TestCaseService;

@RestController
@RequestMapping("/testcases")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
public class TestcaseController {

    private final TestCaseService testCaseService;

    @PostMapping
    public ResponseEntity<ApiResponse<TestCaseExecutionResponse>> createTestCase(
            @Valid @RequestBody TestCaseExecutionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Test case created successfully", testCaseService.createTestCaseByManual(request)));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ExcelUploadResponse>> createTestCaseByUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("featureId") Integer featureId) {
        return ResponseEntity.ok(ApiResponse.success("Excel uploaded successfully", testCaseService.createTestCaseByUpload(file, featureId)));
    }

    @GetMapping("/template/{projectId}/{featureId}")
    public ResponseEntity<byte[]> downloadTemplate(
            @PathVariable Integer projectId,
            @PathVariable Integer featureId) {
        byte[] excelBytes = testCaseService.downloadTemplate(projectId, featureId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=testcase_template_" + featureId + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TestCaseResponse>>> getAllTestCases(
            @RequestParam(required = false) Integer featureId,
            @RequestParam(required = false) TestCaseStatus status,
            @RequestParam(required = false) TestType type,
            @RequestParam(required = false) TestPriority priority,
            @PageableDefault(page = 0, size = 10, sort = "testcaseId") Pageable pageable) {
        if (pageable.getPageSize() <= 0) {
            pageable = PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
        }
        return ResponseEntity.ok(
                ApiResponse.success("Test cases retrieved successfully", testCaseService.getAll(featureId, status, type, priority, pageable))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TestCaseResponse>> getTestCaseById(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(ApiResponse.success("Test case retrieved successfully", testCaseService.getById(id, includeInactive)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<TestCaseResponse>>> searchTestCases(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer featureId,
            @RequestParam(required = false) TestCaseStatus status,
            @RequestParam(required = false) TestType type,
            @RequestParam(required = false) TestPriority priority,
            @PageableDefault(page = 0, size = 10, sort = "testcaseId") Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.success("Test cases search results", testCaseService.searchTestCases(keyword, featureId, status, type, priority, pageable))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TestCaseResponse>> updateTestCaseDetails(
            @Valid @RequestBody TestCasePutRequest testCasePutRequest,
            @PathVariable("id") Integer id) {
        TestCaseResponse response = testCaseService.updateTestcaseDetails(testCasePutRequest, id);
        return ResponseEntity.ok(ApiResponse.success("Test case updated successfully", response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> patchTestCaseDetails(
            @RequestBody TestCasePatchRequest testCasePatchRequest,
            @PathVariable("id") Integer id) {
        String response = testCaseService.patchTestCaseDetails(testCasePatchRequest, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/soft/{id}")
    public ResponseEntity<ApiResponse<PatchTestCaseDeleteResponse>> softDelete(
            @PathVariable("id") Integer id) {
        PatchTestCaseDeleteResponse response = testCaseService.softDeleteTestCase(id);
        return ResponseEntity.ok(ApiResponse.success("Test case soft deleted successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> hardDeleteTestCase(
            @PathVariable("id") Integer id) {
        String response = testCaseService.hardDeleteTestCase(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
