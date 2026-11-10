package com.quickbite.cartservice.controller;

import com.quickbite.cartservice.dto.CartItem;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    // Luu tam trong bo nho: bai tap tap trung vao CI, khong can database
    private final Map<Long, List<CartItem>> carts = new ConcurrentHashMap<>();

    @GetMapping("/{userId}")
    public Map<String, Object> getCart(@PathVariable Long userId) {
        List<CartItem> items = carts.getOrDefault(userId, List.of());
        BigDecimal total = items.stream().map(CartItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("userId", userId, "items", items, "total", total);
    }

    @PostMapping("/{userId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItem addItem(@PathVariable Long userId, @RequestBody CartItem item) {
        carts.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(item);
        return item;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@PathVariable Long userId) {
        carts.remove(userId);
    }
}
