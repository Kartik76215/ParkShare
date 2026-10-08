package com.parkshare.controller;

import com.parkshare.dto.BookingRequest;
import com.parkshare.entity.Booking;
import com.parkshare.service.BookingService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public Booking createBooking(@RequestBody BookingRequest request) {
        return bookingService.createBooking(request);
    }

    @GetMapping("/{id}")
    public Booking getBooking(@PathVariable Long id) {
        return bookingService.getBooking(id);
    }

    @GetMapping("/driver/{driverId}")
    public List<Booking> getDriverBookings(@PathVariable Long driverId) {
        return bookingService.getDriverBookings(driverId);
    }

    @GetMapping("/host/{hostId}")
    public List<Booking> getHostBookings(@PathVariable Long hostId) {
        return bookingService.getHostBookings(hostId);
    }

    @PutMapping("/{id}/cancel")
    public Booking cancelBooking(@PathVariable Long id, @RequestParam Long driverId) {
        return bookingService.cancelBooking(id, driverId);
    }

    @PutMapping("/{id}/complete")
    public Booking completeBooking(@PathVariable Long id) {
        return bookingService.completeBooking(id);
    }
}
