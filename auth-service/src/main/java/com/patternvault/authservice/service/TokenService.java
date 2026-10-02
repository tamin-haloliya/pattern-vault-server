package com.patternvault.authservice.service;

import com.patternvault.authservice.entity.RefreshToken;
import com.patternvault.authservice.entity.User;
import com.patternvault.authservice.repository.RefreshTokenRepository;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.patternvault.authservice.util.TokenUtil.generateRawToken;
import static com.patternvault.authservice.util.TokenUtil.sha256;

@Service
public class TokenService {


    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenService(JwtEncoder jwtEncoder, RefreshTokenRepository refreshTokenRepository) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String createAccessToken(User user){
        Instant now = Instant.now();

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claimsSet)).getTokenValue();
    }

    public String createRefreshToken(User user){
        String rawToken = generateRawToken();
        String tokenHash = sha256(rawToken);

        RefreshToken token = new RefreshToken(user, tokenHash, Instant.now().plus(7, ChronoUnit.DAYS));
        refreshTokenRepository.save(token);

        return rawToken;
    }
}
