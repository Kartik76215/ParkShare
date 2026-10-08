package com.parkshare.service;

import com.parkshare.dto.BookingRequest;
import com.parkshare.entity.Availability;
import com.parkshare.entity.Booking;
import com.parkshare.entity.BookingStatus;
import com.parkshare.entity.ParkingSpace;
import com.parkshare.entity.User;
import com.parkshare.entity.UserRole;
import com.parkshare.exception.BadRequestException;
import com.parkshare.exception.ResourceNotFoundException;
import com.parkshare.repository.AvailabilityRepository;
import com.parkshare.repository.BookingRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final AvailabilityRepository availabilityRepository;
    private final ParkingService parkingService;
    private final UserService userService;

    public BookingService(
            BookingRepository bookingRepository,
            AvailabilityRepository availabilityRepository,
            ParkingService parkingService,
            UserService userService
    ) {
        this.bookingRepository = bookingRepository;
        this.availabilityRepository = availabilityRepository;
        this.parkingService = parkingService;
        this.userService = userService;
    }

    public Booking createBooking(BookingRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Booking start time must be before end time.");
        }

        ParkingSpace parkingSpace = parkingService.getParkingSpace(request.getParkingSpaceId());
        User driver = userService.getUser(request.getDriverId());
        if (driver.getRole() != UserRole.DRIVER) {
            throw new BadRequestException("Only drivers can book parking spaces.");
        }

        checkAvailability(request);

        Booking booking = new Booking();
        booking.setParkingSpace(parkingSpace);
        booking.setDriver(driver);
        booking.setBookingDate(request.getBookingDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setTotalPrice(calculatePrice(request, parkingSpace));
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    public Booking getBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
    }

    public List<Booking> getDriverBookings(Long driverId) {
        return bookingRepository.findByDriverIdOrderByBookingDateDesc(driverId);
    }

    public List<Booking> getHostBookings(Long hostId) {
        return bookingRepository.findByParkingSpaceHostIdOrderByBookingDateDesc(hostId);
    }

    public Booking cancelBooking(Long id, Long driverId) {
        Booking booking = getBooking(id);
        if (!booking.getDriver().getId().equals(driverId)) {
            throw new BadRequestException("Only the driver who booked can cancel this booking.");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("This booking cannot be cancelled.");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    public Booking completeBooking(Long id) {
        Booking booking = getBooking(id);
        booking.setStatus(BookingStatus.COMPLETED);
        return bookingRepository.save(booking);
    }

    private BigDecimal calculatePrice(BookingRequest request, ParkingSpace parkingSpace) {
        long minutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        return parkingSpace.getPricePerHour().multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }

    private void checkAvailability(BookingRequest request) {
        List<Availability> availabilityList = availabilityRepository
                .findByParkingSpaceIdAndDate(request.getParkingSpaceId(), request.getBookingDate());

        boolean insideAvailableTime = availabilityList.stream().anyMatch(availability ->
                !request.getStartTime().isBefore(availability.getStartTime())
                        && !request.getEndTime().isAfter(availability.getEndTime())
        );
        if (!insideAvailableTime) {
            throw new BadRequestException("Parking space is not available for the selected time.");
        }

        List<Booking> existingBookings = bookingRepository.findByParkingSpaceIdAndBookingDateAndStatusIn(
                request.getParkingSpaceId(),
                request.getBookingDate(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );

        boolean hasOverlap = existingBookings.stream().anyMatch(existing ->
                request.getStartTime().isBefore(existing.getEndTime())
                        && request.getEndTime().isAfter(existing.getStartTime())
        );
        if (hasOverlap) {
            throw new BadRequestException("Selected time conflicts with an existing booking.");
        }
    }
}
