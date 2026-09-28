package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelUploadResponse;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.service.TestCaseService;


@RestController
@RequiredArgsConstructor
public class TestcaseController {

    private final TestCaseService testCaseService;

    // Manual test case creation
    @PostMapping
    public ResponseEntity<TestCaseExecutionResponse> createTestCaseByManual(
            @Valid @RequestBody TestCaseExecutionRequest request) {

        TestCaseExecutionResponse response =
                testCaseService.createTestCaseByManual(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Excel test case upload
    @PostMapping(
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
    }


}
