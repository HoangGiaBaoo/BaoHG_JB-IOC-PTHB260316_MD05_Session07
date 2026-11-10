package com.quickbite.restaurantservice.controller;

import com.quickbite.restaurantservice.dto.Restaurant;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final List<Restaurant> restaurants = List.of(
            new Restaurant(1L, "QuickBite Com Tam", "12 Nguyen Trai, Quan 5", true),
            new Restaurant(2L, "QuickBite Pho Bo", "45 Le Loi, Quan 1", true),
            new Restaurant(3L, "QuickBite Banh Mi", "7 Tran Hung Dao, Quan 1", false)
    );

    @GetMapping
    public List<Restaurant> findAll(@RequestParam(required = false) Boolean open) {
        if (open == null) {
            return restaurants;
        }
        return restaurants.stream().filter(r -> r.open() == open).toList();
    }

    @GetMapping("/{id}")
    public Restaurant findById(@PathVariable Long id) {
        return restaurants.stream()
                .filter(r -> r.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant " + id + " khong ton tai"));
    }
}
