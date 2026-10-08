package com.parkshare.repository;

import com.parkshare.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByParkingSpaceId(Long parkingSpaceId);
    boolean existsByBookingId(Long bookingId);
}
