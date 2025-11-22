package com.rickbot.healing_backend.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@ConditionalOnProperty(name = "spring.redis.host")
public class RedisConfig {

    @Bean
        public RedisConnectionFactory redisConnectionFactory(RedisProperties props,
                                                                                                                 @org.springframework.beans.factory.annotation.Value("${spring.redis.ssl:false}") boolean useSsl) {
                // Use standalone config for Lettuce and support optional username/password
                org.springframework.data.redis.connection.RedisStandaloneConfiguration cfg =
                                new org.springframework.data.redis.connection.RedisStandaloneConfiguration();
                cfg.setHostName(props.getHost());
                Integer port = props.getPort();
                cfg.setPort(port == null ? 6379 : port);
                if (props.getPassword() != null && !props.getPassword().isEmpty()) {
                        cfg.setPassword(props.getPassword());
                }
                if (props.getUsername() != null && !props.getUsername().isEmpty()) {
                        cfg.setUsername(props.getUsername());
                }

                org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
                                org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.builder();

                if (useSsl) {
                        builder.useSsl();
                }

                org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration clientConfig = builder.build();

                return new LettuceConnectionFactory(cfg, clientConfig);
        }

    @Bean
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }
}
