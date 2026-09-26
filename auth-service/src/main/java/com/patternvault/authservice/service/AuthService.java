package com.patternvault.authservice.service;

import com.patternvault.authservice.dto.*;
import com.patternvault.authservice.entity.RefreshToken;
import com.patternvault.authservice.entity.User;
import com.patternvault.authservice.exception.ConflictException;
import com.patternvault.authservice.repository.RefreshTokenRepository;
import com.patternvault.authservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static com.patternvault.authservice.util.TokenUtil.generateRawToken;
import static com.patternvault.authservice.util.TokenUtil.sha256;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email already exists.");
        }

        if(userRepository.existsByUsername(req.username())){
            throw new ConflictException("Username already taken.");
        }

        User user = new User(req.username(), req.email(), passwordEncoder.encode(req.password()));
        User saved = userRepository.save(user);

        return new RegisterResponse(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getCreatedAt());
    }

    public TokenResponse login(LoginRequest req){
        User user = userRepository.findByEmail(req.email()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials."));

        if(!passwordEncoder.matches(req.password(), user.getPasswordHashed())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
        }

        String accessToken = createAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest req){
        RefreshToken token = refreshTokenRepository.findByTokenHash(sha256(req.refreshToken())).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized user"));

        if(token.isRevoked() || token.getExpireAt().isBefore(Instant.now())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized user");
        }

        token.setRevoked(true);

        String newAccessToken = createAccessToken(token.getUser());
        String newRefreshToken = createRefreshToken(token.getUser());

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    public void logout(LogoutRequest req){
        RefreshToken token = refreshTokenRepository.findByTokenHash(sha256(req.refreshToken())).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized user"));

        if(token.isRevoked() || token.getExpireAt().isBefore(Instant.now())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized user");
        }

        token.setRevoked(true);

        refreshTokenRepository.save(token);
    }

    @Transactional
    public void logoutAll(UUID id){
        refreshTokenRepository.revokeAllRefreshTokens(id);
    }

    private String createAccessToken(User user){
        Instant now = Instant.now();

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claimsSet)).getTokenValue();
    }

    private String createRefreshToken(User user){
        String rawToken = generateRawToken();
        String tokenHash = sha256(rawToken);

        RefreshToken token = new RefreshToken(user, tokenHash, Instant.now().plus(7, ChronoUnit.DAYS));
        refreshTokenRepository.save(token);

        return rawToken;
    }
}
