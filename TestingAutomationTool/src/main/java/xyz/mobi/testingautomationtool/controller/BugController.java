package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;
import xyz.mobi.testingautomationtool.dto.BugDTO.BugRequest;
import xyz.mobi.testingautomationtool.dto.BugDTO.BugResponse;
import xyz.mobi.testingautomationtool.service.BugService;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/bugs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
public class BugController {

    private final BugService bugService;

    @PostMapping
    public ResponseEntity<xyz.mobi.testingautomationtool.dto.BugDTO.BugResponse> createBug(
            @Valid @RequestBody BugRequest request) {

        BugResponse response = bugService.createBug(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    private final BugService bugService;
    @GetMapping("/{bugId}")
    public ResponseEntity<xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse> getById(
            @PathVariable Integer bugId) {

        return ResponseEntity.ok(
                bugService.getById(bugId));
    }

    @GetMapping
    public ResponseEntity<Page<xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse>> getAllBugs(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        return ResponseEntity.ok(
                bugService.getAllBugs(page, size));
    }
    @GetMapping("/search")
    public ResponseEntity<Page<xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse>> globalSearch(

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            BugSeverity severity,

            @RequestParam(required = false)
            BugPriority priority,

            @RequestParam(required = false)
            BugStatus status,

            @RequestParam(required = false)
            BugCategory category,

            @RequestParam(required = false)
            Integer bugOccurrence,

            @RequestParam(required = false)
            Boolean isActive,

            @RequestParam(required = false)
            LocalDate resolvedFrom,

            @RequestParam(required = false)
            String executedBy,

            @RequestParam(required = false)
            String assignedTo,

            @RequestParam(required = false)
            String updatedBy,

            @RequestParam(required = false)
            LocalDate resolvedTo,

            @RequestParam(required = false)
            String timeZone,

            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject
            Pageable pageable
    ) {

        Page<xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse> response =
                bugService.globalSearch(
                        keyword,
                        severity,
                        priority,
                        status,
                        category,
                        bugOccurrence,
                        isActive,
                        resolvedFrom,
                        resolvedTo,
                        timeZone,
                        pageable,
                        executedBy,
                        assignedTo,
                        updatedBy
                );

        return ResponseEntity.ok(response);
    }

}