package xyz.mobi.testingautomationtool.service;

import org.springframework.security.access.prepost.PreAuthorize;
import xyz.mobi.testingautomationtool.dto.DashboardDto.*;

import java.util.List;

@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public interface DashboardService {
    PatchResponseForManager userConfirmation(PatchRequestOfManager request);

    String userRejection(Integer userId);

    List<MangerGetResponseOfUserEntity> getAllUsers();

    List<MangerGetResponseOfUserEntity> getUsers();

    ManagerDashboardOverviewResponse getManagerOverview();

    ManagerProjectDashboardResponse getProjectDashboard(Integer projectId);

    ManagerFeatureDetailMetrics getFeatureDashboard(Integer featureId);
}
