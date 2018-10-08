package com.yoruichi.ratelimiter.config;


import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

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
            @Value("${spring.redis.host:localhost}") String host,
            @Value("${spring.redis.port:6379}") int port,
            @Value("${spring.redis.database.rateLimiter:0}") int db,
            @Value("${spring.redis.password:}") String password
    ) {
        JedisConnectionFactory jedis = new JedisConnectionFactory();
        jedis.setHostName(host);
        jedis.setPort(port);
        jedis.setDatabase(db);
        if (!Strings.isNullOrEmpty(password)) {
            jedis.setPassword(password);
        }

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(8);
        poolConfig.setMaxWaitMillis(-1);
        poolConfig.setTestOnBorrow(true);

        jedis.setPoolConfig(poolConfig);
        jedis.afterPropertiesSet();
        RedisTemplate<String, List<Long>> template = new RedisTemplate();
        template.setConnectionFactory(jedis);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        log.debug("Create redis template with url {}:{} pwd {}",
                ((JedisConnectionFactory) template.getConnectionFactory()).getHostName(),
                ((JedisConnectionFactory) template.getConnectionFactory()).getPort(),
                ((JedisConnectionFactory) template.getConnectionFactory()).getPassword());
        return template;
    }

}
