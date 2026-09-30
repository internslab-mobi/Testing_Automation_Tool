package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.request.managerRequest.PatchRequestOfManager;
import xyz.mobi.testingautomationtool.dto.response.managerResponse.PatchResponseForManager;
import xyz.mobi.testingautomationtool.service.DashboardService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/manager")
public class DashboardController {

    private final DashboardService managerService;

    @PutMapping("/userConfirmation")
    public ResponseEntity<PatchResponseForManager> userConfirmation(@RequestBody PatchRequestOfManager request) {

        return ResponseEntity.ok(
                managerService.userConfirmation(request));
    }

    @DeleteMapping("/userRejection/{userId}")
    public ResponseEntity<String> userRejection(@PathVariable("userId") Integer userId) {

        return ResponseEntity.ok(
                managerService.userRejection(userId));
    }

}
