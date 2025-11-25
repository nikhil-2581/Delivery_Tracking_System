package com.cryptic.userservice.controller;

import com.cryptic.userservice.dto.request.UpdateDriverStatusRequest;
import com.cryptic.userservice.dto.response.DriverResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Slf4j
public class DriverController {

    private final DriverService driverService;

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable Long id) {
        log.info("Get driver by ID request: {}", id);
        return ResponseEntity.ok(driverService.getDriverById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<DriverResponse> getDriverByUserId(@PathVariable Long userId) {
        log.info("Get driver by user ID request: {}", userId);
        return ResponseEntity.ok(driverService.getDriverByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {
        log.info("Get all drivers request");
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DriverResponse>> getDriversByStatus(
            @PathVariable Driver.DriverStatus status) {
        log.info("Get drivers by status request: {}", status);
        return ResponseEntity.ok(driverService.getDriversByStatus(status));
    }

    @GetMapping("/available")
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers() {
        log.info("Get available drivers request");
        return ResponseEntity.ok(driverService.getAvailableDrivers());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DriverResponse> updateDriverStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDriverStatusRequest request) {
        log.info("Update driver status request: {} to {}", id, request.getStatus());
        return ResponseEntity.ok(driverService.updateDriverStatus(id, request));
    }

    @PostMapping("/{id}/assign-order")
    public ResponseEntity<String> assignOrder(
            @PathVariable Long id,
            @RequestParam Long orderId) {
        log.info("Assign order {} to driver {}", orderId, id);
        driverService.assignOrderToDriver(id, orderId);
        return ResponseEntity.ok("Order assigned successfully");
    }

    @PostMapping("/{id}/complete-order")
    public ResponseEntity<String> completeOrder(@PathVariable Long id) {
        log.info("Complete order for driver: {}", id);
        driverService.completeOrder(id);
        return ResponseEntity.ok("Order completed successfully");
    }
}
