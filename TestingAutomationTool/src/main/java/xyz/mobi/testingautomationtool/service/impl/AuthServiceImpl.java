package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xyz.mobi.testingautomationtool.dto.request.auth.ChangePasswordRequest;
import xyz.mobi.testingautomationtool.dto.request.auth.LoginRequest;
import xyz.mobi.testingautomationtool.dto.request.auth.RegisterRequest;
import xyz.mobi.testingautomationtool.dto.response.auth.AuthResponse;
import xyz.mobi.testingautomationtool.dto.response.auth.UserProfileResponse;
import xyz.mobi.testingautomationtool.entity.Role;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.repository.RoleRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.security.CustomUserDetails;
import xyz.mobi.testingautomationtool.security.JwtUtils;
import xyz.mobi.testingautomationtool.service.AuthService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public AuthResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .designation(request.getDesignation())
                .skills(request.getSkills())
                .role(null)
                .isActive(false)
                .build();

        User savedUser = userRepository.save(user);


        return AuthResponse.builder()
                .token(null)
                .type("Bearer")
                .userId(savedUser.getUserId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .designation(savedUser.getDesignation())
                .message("User registered successfully")
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsernameOrEmail().trim();

        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password"));

        if (!user.isActive()) {
            throw new IllegalStateException("User account is inactive. Please contact your administrator.");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
            );
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", identifier);
            throw new BadCredentialsException("Invalid username/email or password");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String token = jwtUtils.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(userDetails.getUserId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .role(userDetails.getRoleName())
                .fullName(userDetails.getFullName())
                .designation(user.getDesignation())
                .message("User logged in successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .designation(user.getDesignation())
                .skills(user.getSkills())
                .role(user.getRole() != null ? user.getRole().getRole() : null)
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new IllegalStateException("User is not authenticated");
        }

        return userDetails.getUser();
    }
}
