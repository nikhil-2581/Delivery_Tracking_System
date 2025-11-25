package com.cryptic.userservice.repository;

import com.cryptic.userservice.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByUserId(Long userId);

    List<Driver> findByStatus(Driver.DriverStatus status);

    boolean existsByLicenseNo(String licenseNo);

    @Query("SELECT d FROM Driver d WHERE d.status = 'ONLINE' AND d.currentOrderId IS NULL")
    List<Driver> findAvailableDrivers();

    @Query("SELECT d FROM Driver d JOIN FETCH d.user WHERE d.status = :status")
    List<Driver> findByStatusWithUser(Driver.DriverStatus status);
}
