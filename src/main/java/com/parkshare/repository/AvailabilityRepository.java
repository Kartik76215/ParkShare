package com.parkshare.repository;

import com.parkshare.entity.Availability;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByParkingSpaceId(Long parkingSpaceId);
    List<Availability> findByParkingSpaceIdAndDate(Long parkingSpaceId, LocalDate date);
}
