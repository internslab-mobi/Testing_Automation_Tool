package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.request.managerRequest.PatchRequestOfManager;
import xyz.mobi.testingautomationtool.dto.response.managerResponse.MangerGetResponseOfUserEntity;
import xyz.mobi.testingautomationtool.dto.response.managerResponse.PatchResponseForManager;

import java.util.List;

public interface DashboardService {
    PatchResponseForManager userConfirmation(PatchRequestOfManager request);

    String userRejection(Integer userId);

    List<MangerGetResponseOfUserEntity> getAllUsers();

    List<MangerGetResponseOfUserEntity> getUsers();
}
