package com.patternvault.authservice.dto;

import java.time.Instant;
import java.util.UUID;

public record RegisterResponse(UUID id, String username, String email, Instant createdAt) {}
