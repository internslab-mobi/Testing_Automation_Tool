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
import xyz.mobi.testingautomationtool.dto.AuthDTO.*;
import xyz.mobi.testingautomationtool.entity.PasswordResetOtp;
import xyz.mobi.testingautomationtool.entity.RefreshToken;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.exception.AccountDisabledException;
import xyz.mobi.testingautomationtool.exception.DuplicateResourceException;
import xyz.mobi.testingautomationtool.exception.EmailNotFoundException;
import xyz.mobi.testingautomationtool.exception.InvalidTokenException;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.repository.PasswordResetOtpRepository;
import xyz.mobi.testingautomationtool.repository.RefreshTokenRepository;
import xyz.mobi.testingautomationtool.repository.RoleRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.security.CustomUserDetails;
import xyz.mobi.testingautomationtool.security.JwtUtils;
import xyz.mobi.testingautomationtool.service.AuthService;
import xyz.mobi.testingautomationtool.service.EmailService;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

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
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final EmailService emailService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username '" + username + "' is already taken");
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email '" + email + "' is already registered");
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
                .userId(savedUser.getUserId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .designation(savedUser.getDesignation())
                .message("User registered successfully. Awaiting manager approval.")
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsernameOrEmail().trim();

        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password"));

        if (!user.isActive()) {
            throw new AccountDisabledException("User account is inactive. Please contact your administrator.");
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

        String accessToken = jwtUtils.generateAccessToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        // Delete any existing refresh token for this user before storing new one
        refreshTokenRepository.deleteByUserId(user.getUserId());
        refreshTokenRepository.flush();

        // Save new RefreshToken record to database
        RefreshToken tokenEntity = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiryDate(Instant.now().plusMillis(jwtUtils.getRefreshTokenExpirationMs()))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(tokenEntity);

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtils.getAccessTokenExpirationMs() / 1000)
                .refreshTokenExpiresIn(jwtUtils.getRefreshTokenExpirationMs() / 1000)
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
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token cannot be blank");
        }

        try {
            // Step 1: Validate JWT cryptographic signature and format
            if (!jwtUtils.validateRefreshToken(refreshToken)) {
                throw new BadCredentialsException("Invalid refresh token. Session has expired. Please login again.");
            }

            // Step 2: Validate token in database (verify exists, not revoked, and not expired)
            RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                    .orElseThrow(() -> new BadCredentialsException("Revoked or untracked refresh token. Session has expired. Please login again."));

            if (tokenEntity.isRevoked() || tokenEntity.isExpired()) {
                throw new BadCredentialsException("Session has expired. Please login again.");
            }

            String username = jwtUtils.getUsernameFromToken(refreshToken);
            User user = userRepository.findByUsernameOrEmail(username, username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

            if (!user.isActive()) {
                throw new AccountDisabledException("User account is inactive. Please contact your administrator.");
            }

            CustomUserDetails userDetails = new CustomUserDetails(user);

            // Generate fresh 15-minute access token
            String newAccessToken = jwtUtils.generateAccessToken(userDetails);

            // Calculate remaining validity of refresh token
            long remainingMs = jwtUtils.getRemainingExpirationMs(refreshToken);
            long remainingMinutes = remainingMs / (60 * 1000);

            String warningMessage = null;
            if (remainingMinutes <= 10 && remainingMinutes > 0) {
                warningMessage = "Warning: Your session will expire in " + remainingMinutes + " minute(s). Please login again soon.";
            } else if (remainingMinutes <= 0) {
                throw new BadCredentialsException("Session has expired. Please login again.");
            }

            return AuthResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtUtils.getAccessTokenExpirationMs() / 1000)
                    .refreshTokenExpiresIn(remainingMs / 1000)
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .build();

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Refresh token is expired for session: {}", e.getMessage());
            throw new BadCredentialsException("Session has expired. Please login again.");
        }
    }

    @Override
    public AuthResponse sendPasswordResetOtp(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException("No active user found with email: " + email));

        if (!user.isActive()) {
            throw new AccountDisabledException("User account is currently inactive. Please contact your administrator.");
        }

        // Remove existing OTP for this user if already present in table
        passwordResetOtpRepository.deleteByUserId(user.getUserId());
        passwordResetOtpRepository.flush();

        SecureRandom random = new SecureRandom();
        String otpCode = String.format("%06d", random.nextInt(1_000_000));
        Instant expiryTime = Instant.now().plus(10, ChronoUnit.MINUTES);

        PasswordResetOtp otpEntity = PasswordResetOtp.builder()
                .user(user)
                .email(email)
                .otpCode(otpCode)
                .expiryTime(expiryTime)
                .build();
        passwordResetOtpRepository.save(otpEntity);

        emailService.sendPasswordResetOtpEmail(user.getEmail(), user.getUsername(), otpCode, 10);

        return AuthResponse.builder()
                .email(email)
                .message("Password reset OTP has been sent to your registered email address.")
                .build();
    }

    @Override
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        PasswordResetOtp otpEntity = passwordResetOtpRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired OTP code."));

        if (otpEntity.isExpired() || !otpEntity.getOtpCode().equals(request.getOtpCode().trim())) {
            throw new InvalidTokenException("Invalid or expired OTP code.");
        }

        // Once OTP is verified, delete the OTP from the table
        passwordResetOtpRepository.delete(otpEntity);
        passwordResetOtpRepository.flush();

        return AuthResponse.builder()
                .email(email)
                .message("OTP verified successfully. You may now reset your password.")
                .build();
    }

    @Override
    public AuthResponse resetPasswordWithOtp(ResetPasswordWithOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // If OTP is present in table, validate and consume it
        Optional<PasswordResetOtp> otpOpt = passwordResetOtpRepository.findByEmail(email);
        if (otpOpt.isPresent()) {
            PasswordResetOtp otpEntity = otpOpt.get();
            if (otpEntity.isExpired() || !otpEntity.getOtpCode().equals(request.getOtpCode().trim())) {
                throw new InvalidTokenException("Invalid or expired OTP code. Please request a new OTP.");
            }
            // Consume OTP to prevent replay attacks
            passwordResetOtpRepository.delete(otpEntity);
            passwordResetOtpRepository.flush();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException("No user found with email: " + email));

        // Update password with BCrypt
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete all prior refresh tokens for this user upon password reset
        refreshTokenRepository.deleteByUserId(user.getUserId());
        refreshTokenRepository.flush();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        // Immediately issue new 15-minute access token & 24-hour refresh token
        String accessToken = jwtUtils.generateAccessToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        // Persist new refresh token in database
        RefreshToken tokenEntity = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiryDate(Instant.now().plusMillis(jwtUtils.getRefreshTokenExpirationMs()))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(tokenEntity);

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .expiresIn(jwtUtils.getAccessTokenExpirationMs() / 1000)
                .refreshTokenExpiresIn(jwtUtils.getRefreshTokenExpirationMs() / 1000)
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .message("Password has been reset successfully. You are now logged in.")
                .build();
    }

    @Override
    public AuthResponse logout(LogoutRequest request) {
        String refreshToken = request.getRefreshToken();
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.deleteByToken(refreshToken);
        }
        return AuthResponse.builder()
                .message("Logged out successfully. Session revoked.")
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

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new IllegalStateException("User is not authenticated");
        }

        return userDetails.getUser();
    }
}
