package com.quickbite.paymentservice.dto;

import com.quickbite.paymentservice.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(@NotNull Long orderId, @NotNull BigDecimal amount, @NotNull PaymentMethod method) {
}
