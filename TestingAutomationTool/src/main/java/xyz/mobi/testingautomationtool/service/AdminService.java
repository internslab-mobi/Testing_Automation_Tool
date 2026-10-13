package xyz.mobi.testingautomationtool.service;

import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.AdminDTO.CreateManagerRequest;
import xyz.mobi.testingautomationtool.dto.AdminDTO.ManagerResponse;
import xyz.mobi.testingautomationtool.enums.UserRole;

import java.util.List;

public interface AdminService {

    ManagerResponse createManager(CreateManagerRequest request);

    @Transactional
    ManagerResponse updateManager(
            Integer userId,
            CreateManagerRequest request);

    @Transactional
    String patchManager(
            Integer userId,
            CreateManagerRequest request);

    @Transactional(readOnly = true)
    ManagerResponse getUserById(Integer userId);

    @Transactional(readOnly = true)
    List<ManagerResponse> getAllUsers();

    @Transactional(readOnly = true)
    List<ManagerResponse> getUsersByRole(UserRole role);

    void softDeleteUser(Integer userId);

    void hardDeleteUser(Integer userId);

    void hardDeleteProject(Integer projectId);

    void hardDeleteFeature(Integer featureId);

    void hardDeleteTestCase(Integer testCaseId);

    void hardDeleteBug(Integer bugId);
}