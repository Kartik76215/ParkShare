package com.parkshare.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.parkshare.dto.BookingRequest;
import com.parkshare.entity.Availability;
import com.parkshare.entity.Booking;
import com.parkshare.entity.BookingStatus;
import com.parkshare.entity.ParkingSpace;
import com.parkshare.entity.User;
import com.parkshare.entity.UserRole;
import com.parkshare.entity.VehicleType;
import com.parkshare.exception.BadRequestException;
import com.parkshare.repository.AvailabilityRepository;
import com.parkshare.repository.BookingRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingServiceTest {
    private BookingRepository bookingRepository;
    private AvailabilityRepository availabilityRepository;
    private ParkingService parkingService;
    private UserService userService;
    private BookingService bookingService;

    private ParkingSpace parkingSpace;
    private User driver;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        availabilityRepository = mock(AvailabilityRepository.class);
        parkingService = mock(ParkingService.class);
        userService = mock(UserService.class);
        bookingService = new BookingService(bookingRepository, availabilityRepository, parkingService, userService);

        User host = new User();
        host.setId(7L);
        host.setRole(UserRole.HOST);

        parkingSpace = new ParkingSpace();
        parkingSpace.setId(10L);
        parkingSpace.setHost(host);
        parkingSpace.setActive(true);
        parkingSpace.setVehicleType(VehicleType.CAR);
        parkingSpace.setPricePerHour(new BigDecimal("40.00"));

        driver = new User();
        driver.setId(20L);
        driver.setRole(UserRole.DRIVER);

        Availability availability = new Availability();
        availability.setParkingSpace(parkingSpace);
        availability.setDate(LocalDate.of(2026, 10, 20));
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(18, 0));

        when(parkingService.getParkingSpace(10L)).thenReturn(parkingSpace);
        when(userService.getUser(20L)).thenReturn(driver);
        when(availabilityRepository.findByParkingSpaceIdAndDate(10L, LocalDate.of(2026, 10, 20)))
                .thenReturn(List.of(availability));
        when(bookingRepository.findByParkingSpaceIdAndBookingDateAndStatusIn(eq(10L), eq(LocalDate.of(2026, 10, 20)), anyList()))
                .thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsPartialBookingAndCalculatesFractionalPrice() {
        Booking booking = bookingService.createBooking(request(LocalTime.of(10, 0), LocalTime.of(13, 30)));

        assertEquals(new BigDecimal("40.00"), booking.getHourlyRateSnapshot());
        assertEquals(new BigDecimal("140.00"), booking.getTotalPrice());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    void rejectsOverlappingBooking() {
        Booking existing = new Booking();
        existing.setStartTime(LocalTime.of(10, 0));
        existing.setEndTime(LocalTime.of(12, 0));
        when(bookingRepository.findByParkingSpaceIdAndBookingDateAndStatusIn(eq(10L), eq(LocalDate.of(2026, 10, 20)), anyList()))
                .thenReturn(List.of(existing));

        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(request(LocalTime.of(11, 0), LocalTime.of(13, 0))));
    }

    @Test
    void allowsAdjacentBookingBecauseIntervalsAreHalfOpen() {
        Booking existing = new Booking();
        existing.setStartTime(LocalTime.of(10, 0));
        existing.setEndTime(LocalTime.of(12, 0));
        when(bookingRepository.findByParkingSpaceIdAndBookingDateAndStatusIn(eq(10L), eq(LocalDate.of(2026, 10, 20)), anyList()))
                .thenReturn(List.of(existing));

        Booking booking = bookingService.createBooking(request(LocalTime.of(12, 0), LocalTime.of(13, 0)));

        assertEquals(new BigDecimal("40.00"), booking.getTotalPrice());
    }

    @Test
    void rejectsInvalidThirtyMinuteInterval() {
        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(request(LocalTime.of(10, 0), LocalTime.of(10, 20))));
    }

    @Test
    void rejectsVehicleTypeMismatch() {
        BookingRequest request = request(LocalTime.of(10, 0), LocalTime.of(11, 0));
        request.setVehicleType(VehicleType.SUV);

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(request));
    }

    private BookingRequest request(LocalTime startTime, LocalTime endTime) {
        BookingRequest request = new BookingRequest();
        request.setParkingSpaceId(10L);
        request.setDriverId(20L);
        request.setBookingDate(LocalDate.of(2026, 10, 20));
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setVehicleType(VehicleType.CAR);
        return request;
    }
}
