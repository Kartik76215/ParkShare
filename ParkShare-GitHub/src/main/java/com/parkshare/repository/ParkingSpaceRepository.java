package com.parkshare.repository;

import com.parkshare.entity.ParkingSpace;
import com.parkshare.entity.VehicleType;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {
    List<ParkingSpace> findByActiveTrue();
    List<ParkingSpace> findByHostIdAndActiveTrue(Long hostId);
    List<ParkingSpace> findByAddressContainingIgnoreCaseAndVehicleTypeAndPricePerHourLessThanEqualAndActiveTrue(
            String address,
            VehicleType vehicleType,
            BigDecimal maxPrice
    );
}
