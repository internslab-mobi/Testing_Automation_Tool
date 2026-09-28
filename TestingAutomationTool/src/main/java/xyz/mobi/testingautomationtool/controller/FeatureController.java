package xyz.mobi.testingautomationtool.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import xyz.mobi.testingautomationtool.dto.request.getmethoddto.FeatureSearchRequest;
import xyz.mobi.testingautomationtool.dto.response.getMethodDTO.FeatureResponse;
import xyz.mobi.testingautomationtool.service.FeatureService;

@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureService featureService;

    @GetMapping("/{featureId}")
    public ResponseEntity<FeatureResponse> getFeatureById(
            @PathVariable Integer featureId) {

        return ResponseEntity.ok(
                featureService.getFeatureById(featureId)
        );
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<Page<FeatureResponse>> getFeaturesByProject(
            @PathVariable Integer projectId,

            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "featureId",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        return ResponseEntity.ok(
                featureService.getFeaturesByProject(
                        projectId,
                        pageable
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<FeatureResponse>> searchFeatures(
            @ModelAttribute FeatureSearchRequest request,

            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "featureId",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        return ResponseEntity.ok(
                featureService.searchFeatures(
                        request,
                        pageable
                )
        );
    }
}