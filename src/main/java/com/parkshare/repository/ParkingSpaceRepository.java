package com.parkshare.repository;

import com.parkshare.entity.ParkingSpace;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {
    List<ParkingSpace> findByActiveTrue();
    List<ParkingSpace> findByHostIdAndActiveTrue(Long hostId);
}
