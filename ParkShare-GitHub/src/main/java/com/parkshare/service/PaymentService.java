package com.parkshare.service;

import com.parkshare.dto.PaymentRequest;
import com.parkshare.entity.Booking;
import com.parkshare.entity.Payment;
import com.parkshare.entity.PaymentStatus;
import com.parkshare.exception.BadRequestException;
import com.parkshare.repository.PaymentRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    public PaymentService(PaymentRepository paymentRepository, BookingService bookingService) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
    }

    public Payment pay(PaymentRequest request) {
        if (paymentRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new BadRequestException("Payment is already recorded for this booking.");
        }
        Booking booking = bookingService.getBooking(request.getBookingId());
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDate(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
}
