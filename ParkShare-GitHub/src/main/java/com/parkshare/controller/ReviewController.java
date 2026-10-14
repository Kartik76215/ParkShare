package com.parkshare.controller;

import com.parkshare.dto.ReviewRequest;
import com.parkshare.entity.Review;
import com.parkshare.service.ReviewService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review addReview(@RequestBody ReviewRequest request) {
        return reviewService.addReview(request);
    }

    @GetMapping("/parking/{parkingSpaceId}")
    public List<Review> getParkingReviews(@PathVariable Long parkingSpaceId) {
        return reviewService.getParkingReviews(parkingSpaceId);
    }
}
