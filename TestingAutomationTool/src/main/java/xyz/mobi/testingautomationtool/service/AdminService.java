package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.AdminDTO.CreateManagerRequest;
import xyz.mobi.testingautomationtool.dto.AdminDTO.ManagerResponse;

public interface AdminService {

    ManagerResponse createManager(CreateManagerRequest request);

    void softDeleteUser(Integer userId);

    void hardDeleteUser(Integer userId);

    void hardDeleteProject(Integer projectId);

    void hardDeleteFeature(Integer featureId);

    void hardDeleteTestCase(Integer testCaseId);

    void hardDeleteBug(Integer bugId);
}