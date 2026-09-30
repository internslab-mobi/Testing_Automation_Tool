package xyz.mobi.testingautomationtool.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import xyz.mobi.testingautomationtool.dto.request.getmethoddto.FeatureSearchRequest;
import xyz.mobi.testingautomationtool.dto.response.getMethodDTO.FeatureResponse;
import xyz.mobi.testingautomationtool.service.FeatureService;

@RestController
@RequestMapping("/features")
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

    @GetMapping("/{projectId}/features/{featureId}/template")
    public ResponseEntity<byte[]> downloadTemplate(
            @PathVariable Integer projectId,
            @PathVariable Integer featureId) {

        byte[] excelFile =
                featureService.downloadTemplate(
                        projectId,
                        featureId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Feature_"
                                + featureId
                                + "_TestCases_Bugs.xlsx"
                )
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelFile);
    }

    @GetMapping("/features/{featureId}/download")
        public ResponseEntity<byte[]> downloadFile(
                @PathVariable Integer featureId) {

            return featureService.downloadFile(featureId);
    }
}