package com.patternvault.authservice.controller;

import com.patternvault.authservice.dto.*;
import com.patternvault.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.refresh(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest req){
        this.authService.logout(req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt){
        UUID id = UUID.fromString(Objects.requireNonNull(jwt.getSubject()));
        authService.logoutAll(id);
        return ResponseEntity.noContent().build();
    }
}
