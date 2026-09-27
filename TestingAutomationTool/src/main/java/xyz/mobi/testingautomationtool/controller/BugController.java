package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse;
import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;
import xyz.mobi.testingautomationtool.service.BugService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bugs")
@RequiredArgsConstructor
public class BugController {

    private final BugService bugService;
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
    @GetMapping("/search")
    public ResponseEntity<Page<BugResponse>> globalSearch(

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

        Page<BugResponse> response =
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
                        pageable
                );

        return ResponseEntity.ok(response);
    }
}
