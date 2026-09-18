package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.PackageMethodDto.TestPatchMethodDto;
import xyz.mobi.testingautomationtool.dto.PutMethodDtos.PutMethodDto;
import xyz.mobi.testingautomationtool.dto.PutMethodDtos.PutMethodResponse;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.dto.TestcaseDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.service.TestCaseService;

@RestController
public class TestcaseController {

    private final TestCaseService testCaseService;


    public TestcaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    @PostMapping("/manual")
    public ResponseEntity<TestCaseExecutionResponse> createTestCaseByManual(
            @RequestBody TestCaseExecutionRequest request) {

        TestCaseExecutionResponse response =
                testCaseService.createTestCaseByManual(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @PutMapping("/{id}/updateDetails")
    public ResponseEntity<PutMethodResponse> updateDetails(@Valid @RequestBody PutMethodDto putMethodDto, @PathVariable("id") Integer id)
    {
        PutMethodResponse response = testCaseService.updateTestcaseDetails(putMethodDto, id);
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{id}/updatePatch")
    public ResponseEntity<TestCaseResponse> patchDetails(@RequestBody TestPatchMethodDto testPatchMethodDto, @PathVariable("id") Integer id) {
        TestCaseResponse response = testCaseService.patchTestCaseDetails(testPatchMethodDto, id);
        return ResponseEntity.ok(response);
    }
}
