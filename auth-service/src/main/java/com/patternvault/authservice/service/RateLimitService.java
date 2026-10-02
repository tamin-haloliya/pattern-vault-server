package com.patternvault.authservice.service;

import io.github.bucket4j.*;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class RateLimitService {
    private static final BucketConfiguration AUTH_CONFIG = BucketConfiguration.builder()
            .addLimit(Bandwidth.builder()
                    .capacity(10)
                    .refillGreedy(10, Duration.ofMinutes(10))
                    .build()
            ).build();

    private static final BucketConfiguration USER_CONFIG = BucketConfiguration.builder()
            .addLimit(Bandwidth.builder()
                    .capacity(100)
                    .refillGreedy(100, Duration.ofMinutes(5))
                    .build()
            ).build();

    private final LettuceBasedProxyManager<byte[]> proxyManager;


    public RateLimitService(LettuceBasedProxyManager<byte[]> proxyManager) {
        this.proxyManager = proxyManager;
    }

    public ConsumptionProbe checkAuth(String clientIp){
        return tryConsume("rl:auth:ip:" + clientIp, AUTH_CONFIG);
    }

    public ConsumptionProbe checkUser(String userId){
        return tryConsume("rl:api:id:" + userId, USER_CONFIG);
    }

    public ConsumptionProbe tryConsume(String key, BucketConfiguration configuration){
        Bucket bucket = proxyManager.builder().build(key.getBytes(StandardCharsets.UTF_8), () -> configuration);
        return bucket.tryConsumeAndReturnRemaining(1);
    }
}
