package com.cryptic.userservice.service;

import com.cryptic.userservice.dto.request.UpdateDriverStatusRequest;
import com.cryptic.userservice.dto.response.DriverResponse;
import com.cryptic.userservice.entity.Driver;
import com.cryptic.userservice.entity.User;
import com.cryptic.userservice.exception.ResourceNotFoundException;
import com.cryptic.userservice.repository.DriverRepository;
import com.cryptic.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing Driver operations
 * Handles driver profile management, status updates, and order assignments
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    /**
     * Get driver by driver ID
     * @param id Driver ID
     * @return DriverResponse with user details
     * @throws ResourceNotFoundException if driver or user not found
     */
    @Transactional(readOnly = true)
    public DriverResponse getDriverById(Long id) {
        log.info("Fetching driver by ID: {}", id);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));

        User user = userRepository.findById(driver.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for driver"));

        return mapToResponse(driver, user);
    }

    /**
     * Get driver by user ID
     * @param userId User ID
     * @return DriverResponse
     * @throws ResourceNotFoundException if driver or user not found
     */
    @Transactional(readOnly = true)
    public DriverResponse getDriverByUserId(Long userId) {
        log.info("Fetching driver by user ID: {}", userId);

        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found for user id: " + userId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return mapToResponse(driver, user);
    }

    /**
     * Get all drivers
     * @return List of DriverResponse
     */
    @Transactional(readOnly = true)
    public List<DriverResponse> getAllDrivers() {
        log.info("Fetching all drivers");

        return driverRepository.findAll().stream()
                .map(driver -> {
                    User user = userRepository.findById(driver.getUserId()).orElse(null);
                    return mapToResponse(driver, user);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get drivers by status
     * @param status Driver status (ONLINE, OFFLINE, BUSY, INACTIVE)
     * @return List of DriverResponse
     */
    @Transactional(readOnly = true)
    public List<DriverResponse> getDriversByStatus(Driver.DriverStatus status) {
        log.info("Fetching drivers by status: {}", status);

        return driverRepository.findByStatus(status).stream()
                .map(driver -> {
                    User user = userRepository.findById(driver.getUserId()).orElse(null);
                    return mapToResponse(driver, user);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get available drivers (ONLINE and no current order)
     * This is useful for order assignment
     * @return List of available DriverResponse
     */
    @Transactional(readOnly = true)
    public List<DriverResponse> getAvailableDrivers() {
        log.info("Fetching available drivers (ONLINE and free)");

        return driverRepository.findAvailableDrivers().stream()
                .map(driver -> {
                    User user = userRepository.findById(driver.getUserId()).orElse(null);
                    return mapToResponse(driver, user);
                })
                .collect(Collectors.toList());
    }

    /**
     * Update driver status
     * @param driverId Driver ID
     * @param request Update request with new status
     * @return Updated DriverResponse
     * @throws ResourceNotFoundException if driver not found
     */
    @Transactional
    public DriverResponse updateDriverStatus(Long driverId, UpdateDriverStatusRequest request) {
        log.info("Updating driver status: {} to {}", driverId, request.getStatus());

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        Driver.DriverStatus oldStatus = driver.getStatus();
        driver.setStatus(request.getStatus());
        driver = driverRepository.save(driver);

        log.info("Driver status updated successfully: {} -> {}", oldStatus, request.getStatus());

        User user = userRepository.findById(driver.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToResponse(driver, user);
    }

    /**
     * Assign an order to a driver
     * Sets the driver status to BUSY and associates the order ID
     * @param driverId Driver ID
     * @param orderId Order ID to assign
     * @throws ResourceNotFoundException if driver not found
     */
    @Transactional
    public void assignOrderToDriver(Long driverId, Long orderId) {
        log.info("Assigning order {} to driver {}", orderId, driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        // Check if driver is available
        if (driver.getCurrentOrderId() != null) {
            log.warn("Driver {} already has an active order: {}", driverId, driver.getCurrentOrderId());
        }

        driver.setCurrentOrderId(orderId);
        driver.setStatus(Driver.DriverStatus.BUSY);
        driverRepository.save(driver);

        log.info("Order {} assigned successfully to driver {}", orderId, driverId);
    }

    /**
     * Mark order as completed for a driver
     * Sets driver status back to ONLINE, clears current order, and increments delivery count
     * @param driverId Driver ID
     * @throws ResourceNotFoundException if driver not found
     */
    @Transactional
    public void completeOrder(Long driverId) {
        log.info("Completing order for driver: {}", driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        Long completedOrderId = driver.getCurrentOrderId();

        // Clear current order and update status
        driver.setCurrentOrderId(null);
        driver.setStatus(Driver.DriverStatus.ONLINE);

        // Increment total deliveries
        driver.setTotalDeliveries(driver.getTotalDeliveries() + 1);

        driverRepository.save(driver);

        log.info("Order {} completed for driver {}. Total deliveries: {}",
                completedOrderId, driverId, driver.getTotalDeliveries());
    }

    /**
     * Update driver rating
     * @param driverId Driver ID
     * @param newRating New rating value
     * @throws ResourceNotFoundException if driver not found
     */
    @Transactional
    public void updateDriverRating(Long driverId, Double newRating) {
        log.info("Updating rating for driver {}: {}", driverId, newRating);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        driver.setRating(java.math.BigDecimal.valueOf(newRating));
        driverRepository.save(driver);

        log.info("Driver rating updated successfully: {}", driverId);
    }

    /**
     * Map Driver and User entities to DriverResponse DTO
     * @param driver Driver entity
     * @param user User entity (can be null)
     * @return DriverResponse DTO
     */
    private DriverResponse mapToResponse(Driver driver, User user) {
        return DriverResponse.builder()
                .id(driver.getId())
                .userId(driver.getUserId())
                .name(user != null ? user.getName() : null)
                .email(user != null ? user.getEmail() : null)
                .phone(user != null ? user.getPhone() : null)
                .licenseNo(driver.getLicenseNo())
                .vehicleInfo(driver.getVehicleInfo())
                .status(driver.getStatus().name())
                .currentOrderId(driver.getCurrentOrderId())
                .rating(driver.getRating() != null ? driver.getRating().doubleValue() : null)
                .totalDeliveries(driver.getTotalDeliveries())
                .createdAt(driver.getCreatedAt())
                .build();
    }
}
