package com.quickbite.paymentservice.controller;

import com.quickbite.paymentservice.dto.PaymentRequest;
import com.quickbite.paymentservice.entity.Payment;
import com.quickbite.paymentservice.exception.PaymentException;
import com.quickbite.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Payment pay(@Valid @RequestBody PaymentRequest request) {
        return paymentService.pay(request);
    }

    @PostMapping("/{id}/refund")
    public Payment refund(@PathVariable Long id) {
        return paymentService.refund(id);
    }

    @ExceptionHandler(PaymentException.class)
    public ProblemDetail handlePaymentException(PaymentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }
}
