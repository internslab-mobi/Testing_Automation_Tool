package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelUploadResponse;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.TestCasePatchRequest;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.UpdateExecutionStatusRequest;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.TestCasePutRequest;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.ExecutionStatusResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.TestCasePutResponse;
import xyz.mobi.testingautomationtool.service.TestCaseService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestcaseController {

    private final TestCaseService testCaseService;

    @PutMapping("/{id}")
    public ResponseEntity<TestCasePutResponse> updateTestCaseDetails(
            @Valid @RequestBody TestCasePutRequest testCasePutRequest,
            @PathVariable("id") Integer id) {

        TestCasePutResponse response = testCaseService
                .updateTestcaseDetails(testCasePutRequest, id);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TestCaseResponse> patchTestCaseDetails(
            @RequestBody TestCasePatchRequest testCasePatchRequest,
            @PathVariable("id") Integer id) {

        TestCaseResponse response = testCaseService
                .patchTestCaseDetails(testCasePatchRequest, id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/delete")
    public ResponseEntity<String> softDeleteTestCase(
            @PathVariable("id") Integer id) {

        String response = testCaseService.softDeleteTestCase(id);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> hardDeleteTestCase(
            @PathVariable("id") Integer id) {

        String response = testCaseService.hardDeleteTestCase(id);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/executions/{executionId}/status")
    public ResponseEntity<ExecutionStatusResponse> updateExecutionStatus(
            @PathVariable Integer executionId,
            @Valid @RequestBody UpdateExecutionStatusRequest request) {

        ExecutionStatusResponse response =
                testCaseService.updateExecutionStatus(
                        executionId,
                        request
                );

        return ResponseEntity.ok(response);
    }










 /*   // Manual test case creation
//    @PostMapping("/manual")
//    public ResponseEntity<TestCaseExecutionResponse> createTestCaseByManual(
//            @Valid @RequestBody TestCaseExecutionRequest request) {
//
//        TestCaseExecutionResponse response =
//                testCaseService.createTestCaseByManual(request);
//
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(response);
//    }*/

    // Excel test case upload
    /*@PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<ExcelUploadResponse> createTestCaseByUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("featureId") Integer featureId) {

        ExcelUploadResponse response =
                testCaseService.createTestCaseByUpload(
                        file,
                        featureId
                );

        return ResponseEntity.ok(response);
    }*/



   /* @GetMapping("/{id}")
    public ResponseEntity<TestCaseExecutionResponse> getById(
            @PathVariable int id) {

        return ResponseEntity.ok(
                testCaseService.getById(id)
        );
    }*/

    /*@GetMapping
    public ResponseEntity<List<TestCaseExecutionResponse>> getByAll() {

        return ResponseEntity.ok(
                testCaseService.getByAll()
        );
    }*/

    /*@GetMapping("/feature/{featureId}")
    public ResponseEntity<Page<TestCaseExecutionResponse>> getByFeatureId(
            @PathVariable Integer featureId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        return ResponseEntity.ok(
                testCaseService.getByFeatureId(featureId, page, size)
        );
    }*/
}
