package com.cryptic.userservice.service;

import com.cryptic.userservice.dto.request.LoginRequest;
import com.cryptic.userservice.dto.request.RegisterRequest;
import com.cryptic.userservice.dto.response.AuthResponse;
import com.cryptic.userservice.entity.Driver;
import com.cryptic.userservice.entity.User;
import com.cryptic.userservice.exception.*;
import com.cryptic.userservice.repository.DriverRepository;
import com.cryptic.userservice.repository.UserRepository;
import com.cryptic.userservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        // Validate uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone number already exists: " + request.getPhone());
        }

        // Create user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .hashedPassword(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(User.UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);
        log.info("User created successfully with ID: {}", user.getId());

        // Create driver profile if role is DRIVER
        if (request.getRole() == User.Role.DRIVER) {
            if (request.getLicenseNo() == null || request.getLicenseNo().isBlank()) {
                throw new ValidationException("License number is required for drivers");
            }
            if (driverRepository.existsByLicenseNo(request.getLicenseNo())) {
                throw new DuplicateResourceException("License number already exists: " + request.getLicenseNo());
            }

            Driver driver = Driver.builder()
                    .userId(user.getId())
                    .licenseNo(request.getLicenseNo())
                    .vehicleInfo(request.getVehicleInfo())
                    .status(Driver.DriverStatus.OFFLINE)
                    .build();

            driverRepository.save(driver);
            log.info("Driver profile created for user ID: {}", user.getId());
        }

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name()
        );
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .role(user.getRole().name())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getHashedPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new AccountException("Account is " + user.getStatus() + ". Please contact support.");
        }

        log.info("User logged in successfully: {}", user.getEmail());

        String accessToken = jwtUtil.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name()
        );
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .role(user.getRole().name())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing access token");

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token");
        }

        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new AuthenticationException("Refresh token has expired");
        }

        Long userId = jwtUtil.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new AccountException("Account is not active");
        }

        String newAccessToken = jwtUtil.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name()
        );
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .role(user.getRole().name())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }
}
