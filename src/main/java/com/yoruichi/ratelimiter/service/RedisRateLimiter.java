package com.yoruichi.ratelimiter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author yoruichi
 */

@Slf4j
@Service
public class RedisRateLimiter implements RateLimiter {

    @Autowired
    @Qualifier("rateLimiter")
    private RedisTemplate<String, List<Long>> redisTemplate;

    @Autowired
    private RedisScript<List<Long>> script;

    static List<String> getKeys(String id) {
        // use `{}` around keys to use Redis Key hash tags
        // this allows for using redis cluster

        // Make a unique key per user.
        String prefix = "request_rate_limiter.{" + id;

        // You need two Redis keys for Token Bucket.
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }


    /**
     * This uses a basic token bucket algorithm and relies on the fact that Redis scripts
     * execute atomically. No other operations can run between fetching the count and
     * writing the new count.
     */
    @Override
    public boolean isAllowed(String id, int replenishRate, int burstCapacity, int timeCount, TimeUnit timeUnit, int rateType) {
        try {
            List<String> keys = getKeys(id);
            long seconds = timeUnit.toSeconds(timeCount);

            // The arguments to the LUA script. time() returns unixtime in seconds.
            // allowed, tokens_left = redis.eval(SCRIPT, keys, args)
            List<Long> response = redisTemplate.execute(this.script, keys, replenishRate, burstCapacity, Instant.now().getEpochSecond(), 1, seconds, rateType);
            log.debug("key:{},ratetype:{},allowed_num:{},new_tokens:{},delta:{},last_tokens:{},now:{},last_refreshed:{},ttl_times:{},cal_delta:{}",
                    keys.get(0), rateType, response.get(0), response.get(1), response.get(2), response.get(3), response.get(4), response.get(5), response.get(6), response.get(7));
            return response.get(0) == 1L;
        } catch (Exception e) {
            log.error("Error determining if user allowed from redis", e);
        }
        return true;
    }

}
