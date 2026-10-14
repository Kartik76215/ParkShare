package com.parkshare.repository;

import com.parkshare.entity.Booking;
import com.parkshare.entity.BookingStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByDriverIdOrderByBookingDateDesc(Long driverId);
    List<Booking> findByParkingSpaceHostIdOrderByBookingDateDesc(Long hostId);
    List<Booking> findByParkingSpaceIdAndBookingDateAndStatusIn(
            Long parkingSpaceId,
            LocalDate bookingDate,
            List<BookingStatus> statuses
    );
}
