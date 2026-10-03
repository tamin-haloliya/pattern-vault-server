package com.patternvault.authservice.dto;

import com.patternvault.authservice.entity.User;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UserResponse(@NotNull String userName, @NotNull String email, @NotNull Instant createdAt) {

    public static UserResponse from(User user){
        return new UserResponse(user.getUsername(), user.getEmail(), user.getCreatedAt());
    }
}
