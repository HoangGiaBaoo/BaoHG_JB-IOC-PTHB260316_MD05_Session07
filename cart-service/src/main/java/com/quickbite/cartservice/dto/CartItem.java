package com.quickbite.cartservice.dto;

import java.math.BigDecimal;

public record CartItem(Long menuItemId, String name, int quantity, BigDecimal unitPrice) {

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
