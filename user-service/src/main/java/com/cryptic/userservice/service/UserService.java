package com.cryptic.userservice.service;

import com.cryptic.userservice.dto.request.ChangePasswordRequest;
import com.cryptic.userservice.dto.request.UpdateUserRequest;
import com.cryptic.userservice.dto.response.UserResponse;
import com.cryptic.userservice.entity.User;
import com.cryptic.userservice.exception.AuthenticationException;
import com.cryptic.userservice.exception.DuplicateResourceException;
import com.cryptic.userservice.exception.ResourceNotFoundException;
import com.cryptic.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing User operations
 * Handles CRUD operations, password management, and user status updates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get user by ID
     * @param id User ID
     * @return UserResponse
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    /**
     * Get user by email
     * @param email User email
     * @return UserResponse
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToResponse(user);
    }

    /**
     * Get all users
     * @return List of UserResponse
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get users by role
     * @param role User role (CUSTOMER, DRIVER, ADMIN)
     * @return List of UserResponse
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(User.Role role) {
        log.info("Fetching users by role: {}", role);
        return userRepository.findByRole(role).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update user profile
     * @param id User ID
     * @param request Update request with new name and/or phone
     * @return Updated UserResponse
     * @throws ResourceNotFoundException if user not found
     * @throws DuplicateResourceException if phone already exists
     */
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Update name if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            log.debug("Updating name for user {}: {} -> {}", id, user.getName(), request.getName());
            user.setName(request.getName());
        }

        // Update phone if provided
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            // Check if phone is already taken by another user
            if (!request.getPhone().equals(user.getPhone()) &&
                    userRepository.existsByPhone(request.getPhone())) {
                throw new DuplicateResourceException("Phone number already exists: " + request.getPhone());
            }
            log.debug("Updating phone for user {}: {} -> {}", id, user.getPhone(), request.getPhone());
            user.setPhone(request.getPhone());
        }

        user = userRepository.save(user);
        log.info("User updated successfully: {}", id);

        return mapToResponse(user);
    }

    /**
     * Change user password
     * @param id User ID
     * @param request Change password request with current and new password
     * @throws ResourceNotFoundException if user not found
     * @throws AuthenticationException if current password is incorrect
     */
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getHashedPassword())) {
            log.warn("Password change failed for user {}: incorrect current password", id);
            throw new AuthenticationException("Current password is incorrect");
        }

        // Hash and save new password
        String hashedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setHashedPassword(hashedNewPassword);
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", id);
    }

    /**
     * Update user status (ACTIVE, INACTIVE, SUSPENDED)
     * @param id User ID
     * @param status New status
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public void updateUserStatus(Long id, User.UserStatus status) {
        log.info("Updating status for user {}: {}", id, status);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setStatus(status);
        userRepository.save(user);

        log.info("User status updated successfully for user: {}", id);
    }

    /**
     * Delete user by ID
     * @param id User ID
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }

    /**
     * Map User entity to UserResponse DTO
     * @param user User entity
     * @return UserResponse DTO
     */
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
