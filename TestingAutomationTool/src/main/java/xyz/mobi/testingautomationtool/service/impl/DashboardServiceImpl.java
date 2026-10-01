package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.DashboardDto.MangerGetResponseOfUserEntity;
import xyz.mobi.testingautomationtool.dto.DashboardDto.PatchRequestOfManager;
import xyz.mobi.testingautomationtool.dto.DashboardDto.PatchResponseForManager;
import xyz.mobi.testingautomationtool.entity.Role;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.repository.RoleRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.DashboardService;
import xyz.mobi.testingautomationtool.service.EmailService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@PreAuthorize("hasRole('MANAGER')")
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

    @Override
    public PatchResponseForManager userConfirmation(PatchRequestOfManager request) {
        if (request.getRole() == null || request.getUserId() == null) {
            throw new IllegalArgumentException("UserId and Role are required");
        }

        User user = userRepository.findByUserIdAndIsActiveFalse(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found or is already active with ID: " + request.getUserId()));

        Role role = roleRepository.findByRole(String.valueOf(request.getRole()))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Role: " + request.getRole()));

        user.setActive(true);
        user.setRole(role);
        userRepository.save(user);

        emailService.confirmationEmail(user.getEmail(), user.getUsername());

        return PatchResponseForManager.builder()
                .message("User has been confirmed and activated successfully")
                .username(user.getUsername())
                .build();
    }

    @Override
    public String userRejection(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        emailService.rejectEmail(user.getEmail(), user.getUsername());
        userRepository.delete(user);

        return "User has been removed successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public List<MangerGetResponseOfUserEntity> getAllUsers() {
        List<User> users = userRepository.findByIsActiveTrue();
        return users.stream()
                .map(user -> MangerGetResponseOfUserEntity.builder()
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .userId(user.getUserId())
                        .fullName(user.getFullName())
                        .role(user.getRole())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MangerGetResponseOfUserEntity> getUsers() {
        List<User> users = userRepository.findByIsActiveFalse();
        return users.stream()
                .map(user -> MangerGetResponseOfUserEntity.builder()
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .userId(user.getUserId())
                        .fullName(user.getFullName())
                        .role(user.getRole())
                        .build())
                .toList();
    }
}
