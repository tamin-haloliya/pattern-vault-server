package com.patternvault.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Getter
    @Column(nullable = false, unique = true)
    private String username;

    @Getter
    @Column(nullable = false, unique = true)
    private String email;

    @Getter
    @Column(name = "password_hashed", nullable = false)
    private String passwordHashed;

    @Getter
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public User(String username, String email, String passwordHashed) {
        this.username = username;
        this.email = email;
        this.passwordHashed = passwordHashed;
    }
}
