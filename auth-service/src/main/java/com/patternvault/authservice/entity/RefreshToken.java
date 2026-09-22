package com.patternvault.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Getter
    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Getter
    @Column(name = "expire_at", nullable = false)
    private Instant expireAt;

    @Getter
    @Setter
    @Column(nullable = false)
    private boolean revoked = false;

    public RefreshToken(User user, String tokenHash, Instant expireAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expireAt = expireAt;
    }
}
