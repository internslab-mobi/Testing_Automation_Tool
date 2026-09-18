package xyz.mobi.testingautomationtool.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.service.TestCaseService;

@Controller
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
}
