package com.yoruichi.ratelimiter.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.List;
import java.util.Objects;

/**
 * @author yoruichi
 */

@Slf4j
@Configuration
public class RedisConfiguration {

    @Bean
    public RedisScript redisRequestRateLimiterScript() {
        DefaultRedisScript redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("META-INF/scripts/request_rate_limiter.lua")));
        redisScript.setResultType(List.class);
        return redisScript;
    }

    @Bean(name = "rateLimiter")
    public RedisTemplate<String, List<Long>> redisTemplate(
            @Value("${spring.redis.host.rateLimiter:localhost}") String host,
            @Value("${spring.redis.port.rateLimiter:6379}") int port,
            @Value("${spring.redis.database.rateLimiter:0}") int db,
            @Value("${spring.redis.password.rateLimiter:}") String password,
            @Value("${spring.redis.pool.max-active.rateLimiter:64}") int maxActive,
            @Value("${spring.redis.pool.max-wait.rateLimiter:-1}") int maxWait,
            @Value("${spring.redis.pool.max-idle.rateLimiter:64}") int maxIdle,
            @Value("${spring.redis.pool.min-idle.rateLimiter:32}") int minIdle
    ) {

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxWaitMillis(maxWait);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(60000L);
        poolConfig.setTimeBetweenEvictionRunsMillis(30000L);
        poolConfig.setNumTestsPerEvictionRun(-1);
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(db);
        if (Objects.nonNull(password) && password.trim().length() > 0) {
            configuration.setPassword(password);
        }

        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(configuration,
                LettucePoolingClientConfiguration.builder().poolConfig(poolConfig).build());
        lettuceConnectionFactory.afterPropertiesSet();
        RedisTemplate template = new StringRedisTemplate(lettuceConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

}
