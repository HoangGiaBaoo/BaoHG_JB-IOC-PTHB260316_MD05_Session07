package com.quickbite.userservice.dto;

public record UserResponse(Long id, String fullName, String email, String phone) {
}
