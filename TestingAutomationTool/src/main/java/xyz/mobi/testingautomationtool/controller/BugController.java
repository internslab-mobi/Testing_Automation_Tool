package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.BugDTO.BugRequest;
import xyz.mobi.testingautomationtool.dto.BugDTO.BugResponse;
import xyz.mobi.testingautomationtool.service.BugService;

@RestController
@RequestMapping("/api/v1/bugs")
@RequiredArgsConstructor
public class BugController {

    private final BugService bugService;

    @PostMapping
    public ResponseEntity<BugResponse> createBug(
            @Valid @RequestBody BugRequest request) {

        BugResponse response = bugService.createBug(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}