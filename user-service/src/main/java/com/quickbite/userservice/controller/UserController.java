package com.quickbite.userservice.controller;

import com.quickbite.userservice.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final Map<Long, UserResponse> users = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @GetMapping
    public Collection<UserResponse> findAll() {
        return users.values();
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        UserResponse user = users.get(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + id + " khong ton tai");
        }
        return user;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody UserResponse request) {
        long id = sequence.incrementAndGet();
        UserResponse user = new UserResponse(id, request.fullName(), request.email(), request.phone());
        users.put(id, user);
        return user;
    }
}
