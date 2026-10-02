package com.patternvault.authservice.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;


@Configuration
public class RateLimitConfig {

    @Value("${spring.data.redis.host}") private String host;
    @Value("${spring.data.redis.port}") private int port;
    @Value("${spring.data.redis.password}") private String password;

    @Bean
    RedisClient client(){
        return RedisClient.create(RedisURI.builder().withHost(host).withPort(port).withPassword(password).build());
    }

    @Bean
    public StatefulRedisConnection<byte[], byte[]> bucket4jRedisConnection(RedisClient client) {
        return client.connect(ByteArrayCodec.INSTANCE);
    }

    @Bean
    public LettuceBasedProxyManager<byte[]> proxyManager(StatefulRedisConnection<byte[], byte[]> connection) {
        return LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(10))
                )
                .build();
    }

}