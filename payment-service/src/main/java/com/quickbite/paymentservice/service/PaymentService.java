package com.quickbite.paymentservice.service;

import com.quickbite.paymentservice.dto.PaymentRequest;
import com.quickbite.paymentservice.entity.Payment;
import com.quickbite.paymentservice.entity.PaymentMethod;
import com.quickbite.paymentservice.entity.PaymentStatus;
import com.quickbite.paymentservice.exception.PaymentException;
import com.quickbite.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BigDecimal maxAmount;

    public PaymentService(PaymentRepository paymentRepository,
                          @Value("${payment.max-amount}") BigDecimal maxAmount) {
        this.paymentRepository = paymentRepository;
        this.maxAmount = maxAmount;
    }

    @Transactional
    public Payment pay(PaymentRequest request) {
        BigDecimal amount = request.amount();
        if (amount.signum() <= 0) {
            throw new PaymentException("So tien thanh toan phai lon hon 0");
        }
        if (amount.compareTo(maxAmount) > 0) {
            throw new PaymentException("So tien vuot han muc " + maxAmount.toPlainString() + " VND");
        }
        if (paymentRepository.existsByOrderIdAndStatus(request.orderId(), PaymentStatus.SUCCESS)) {
            throw new PaymentException("Don hang " + request.orderId() + " da duoc thanh toan");
        }
        BigDecimal fee = calculateFee(request.method(), amount);
        return paymentRepository.save(new Payment(request.orderId(), amount, fee, request.method()));
    }

    @Transactional
    public Payment refund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Giao dich " + paymentId + " khong ton tai"));
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new PaymentException("Chi hoan tien duoc giao dich SUCCESS, hien tai: " + payment.getStatus());
        }
        payment.refund();
        return paymentRepository.save(payment);
    }

    /** Phi giao dich: tien mat 0%, the 1.5%, vi dien tu 1% (lam tron den dong). */
    BigDecimal calculateFee(PaymentMethod method, BigDecimal amount) {
        BigDecimal rate = switch (method) {
            case CASH -> BigDecimal.ZERO;
            case CARD -> new BigDecimal("0.015");
            case E_WALLET -> new BigDecimal("0.01");
        };
        return amount.multiply(rate).setScale(0, RoundingMode.HALF_UP);
    }
}
