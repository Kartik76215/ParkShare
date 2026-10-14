package com.parkshare.controller;

import com.parkshare.dto.PaymentRequest;
import com.parkshare.entity.Payment;
import com.parkshare.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public Payment pay(@RequestBody PaymentRequest request) {
        return paymentService.pay(request);
    }
}
