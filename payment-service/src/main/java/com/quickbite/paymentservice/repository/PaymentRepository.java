package com.quickbite.paymentservice.repository;

import com.quickbite.paymentservice.entity.Payment;
import com.quickbite.paymentservice.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);
}
