package com.cryptic.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String licenseNo;
    private String vehicleInfo;
    private String status;
    private Long currentOrderId;
    private Double rating;
    private Integer totalDeliveries;
    private LocalDateTime createdAt;
}
