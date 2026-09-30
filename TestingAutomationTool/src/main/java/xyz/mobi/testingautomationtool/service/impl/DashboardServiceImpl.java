package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.mobi.testingautomationtool.dto.request.managerRequest.PatchRequestOfManager;
import xyz.mobi.testingautomationtool.dto.response.managerResponse.MangerGetResponseOfUserEntity;
import xyz.mobi.testingautomationtool.dto.response.managerResponse.PatchResponseForManager;
import xyz.mobi.testingautomationtool.entity.Role;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.repository.RoleRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.EmailService;
import xyz.mobi.testingautomationtool.service.DashboardService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

    @Override
    public PatchResponseForManager userConfirmation(PatchRequestOfManager request) {

        if(request.getRole()==null &&
                request.getUserId()==null){
            throw new IllegalStateException("Request not valid");
        }

        User user = userRepository.findByUserIdAndIsActiveFalse(request.getUserId())
                .orElseThrow(
                        ()-> new ResourceNotFoundException("User is already active")
        );

        Role role = roleRepository.findByRole(String.valueOf(request.getRole()))
                        .orElseThrow(
                                ()-> new ResourceNotFoundException("Role Invalid")

        );

        user.setActive(true);
        user.setRole(role);

        userRepository.save(user);

        emailService.confirmationEmail(user.getEmail(),user.getUsername());

        return PatchResponseForManager.builder()
                .message("User has been created successfully")
                .username(user.getUsername())
                .build();
    }

    @Override
    public String userRejection(Integer userId){

        User user = userRepository.findById(userId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("User not found")
                );

        emailService.rejectEmail(user.getEmail(),user.getUsername());

        userRepository.delete(user);

        return "User has been removed successfully";
    }

    @Override
    public List<MangerGetResponseOfUserEntity> getAllUsers() {
        List<User> users = userRepository.findByIsActiveTrue();
        return users.stream()
                .map(user -> MangerGetResponseOfUserEntity.builder()
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .userId(user.getUserId())
                        .fullName(user.getFullName())
                        .role(user.getRole() != null
                                ? user.getRole()
                                : null)
                        .build())
                .toList();
    }


    @Override
    public List<MangerGetResponseOfUserEntity> getUsers() {
        List<User> users = userRepository.findByIsActiveFalse();
        return users.stream()
                .map(user -> MangerGetResponseOfUserEntity.builder()
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .userId(user.getUserId())
                        .fullName(user.getFullName())
                        .role(user.getRole() != null
                                ? user.getRole()
                                : null)
                        .build())
                .toList();
    }
}
