package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelUploadResponse;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.TestCasePatchRequest;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.UpdateExecutionStatusRequest;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.TestCasePutRequest;
import xyz.mobi.testingautomationtool.dto.response.DeleteMethodDto.PatchTestCaseDeleteResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.ExecutionStatusResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.PatchTestCaseResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.TestCasePutResponse;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.TestcaseDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;
import xyz.mobi.testingautomationtool.service.TestCaseService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
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

    @PutMapping("/{id}/update")
    public ResponseEntity<TestCasePutResponse> updateTestCaseDetails(
            @Valid @RequestBody TestCasePutRequest testCasePutRequest,
            @PathVariable("id") Integer id) {

        TestCasePutResponse response = testCaseService
                .updateTestcaseDetails(testCasePutRequest, id);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> patchTestCaseDetails(
            @RequestBody TestCasePatchRequest testCasePatchRequest,
            @PathVariable("id") Integer id) {

        String response = testCaseService
                .patchTestCaseDetails(testCasePatchRequest, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<PatchTestCaseDeleteResponse> softDelete(@PathVariable("id") Integer id){
        PatchTestCaseDeleteResponse response = testCaseService.softDeleteTestCase(id);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> hardDeleteTestCase(
            @PathVariable("id") Integer id) {

        String response = testCaseService.hardDeleteTestCase(id);

        return ResponseEntity.ok(response);
    }
}
