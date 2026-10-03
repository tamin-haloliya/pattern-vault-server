package com.patternvault.authservice.controller;

import com.patternvault.authservice.dto.UserResponse;
import com.patternvault.authservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
        UUID id = UUID.fromString(Objects.requireNonNull(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.FOUND).body(userService.getMe(id));
    }
}
