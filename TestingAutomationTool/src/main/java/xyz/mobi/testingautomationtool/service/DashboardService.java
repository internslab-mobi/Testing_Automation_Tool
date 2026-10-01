package xyz.mobi.testingautomationtool.service;

import org.springframework.security.access.prepost.PreAuthorize;
import xyz.mobi.testingautomationtool.dto.DashboardDto.MangerGetResponseOfUserEntity;
import xyz.mobi.testingautomationtool.dto.DashboardDto.PatchRequestOfManager;
import xyz.mobi.testingautomationtool.dto.DashboardDto.PatchResponseForManager;

import java.util.List;

@PreAuthorize("hasRole('MANAGER')")
public interface DashboardService {
    PatchResponseForManager userConfirmation(PatchRequestOfManager request);

    String userRejection(Integer userId);

    List<MangerGetResponseOfUserEntity> getAllUsers();

    List<MangerGetResponseOfUserEntity> getUsers();
}
