package com.parkshare.service;

import com.parkshare.dto.ReviewRequest;
import com.parkshare.entity.Booking;
import com.parkshare.entity.BookingStatus;
import com.parkshare.entity.ParkingSpace;
import com.parkshare.entity.Review;
import com.parkshare.entity.User;
import com.parkshare.exception.BadRequestException;
import com.parkshare.repository.ReviewRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookingService bookingService;
    private final ParkingService parkingService;
    private final UserService userService;

    public ReviewService(
            ReviewRepository reviewRepository,
            BookingService bookingService,
            ParkingService parkingService,
            UserService userService
    ) {
        this.reviewRepository = reviewRepository;
        this.bookingService = bookingService;
        this.parkingService = parkingService;
        this.userService = userService;
    }

    public Review addReview(ReviewRequest request) {
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5.");
        }
        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new BadRequestException("Review already exists for this booking.");
        }

        Booking booking = bookingService.getBooking(request.getBookingId());
        if (booking.getStatus() != BookingStatus.COMPLETED && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Only completed or confirmed bookings can be reviewed.");
        }
        if (!booking.getDriver().getId().equals(request.getDriverId())) {
            throw new BadRequestException("Only the booking driver can review.");
        }

        User driver = userService.getUser(request.getDriverId());
        ParkingSpace parkingSpace = parkingService.getParkingSpace(request.getParkingSpaceId());

        Review review = new Review();
        review.setBooking(booking);
        review.setDriver(driver);
        review.setParkingSpace(parkingSpace);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return reviewRepository.save(review);
    }

    public List<Review> getParkingReviews(Long parkingSpaceId) {
        return reviewRepository.findByParkingSpaceId(parkingSpaceId);
    }
}
