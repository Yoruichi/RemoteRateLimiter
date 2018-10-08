package com.yoruichi.ratelimiter.bean;

import com.yoruichi.ratelimiter.annotation.RateLimiterPolicy;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

/**
 * @author yoruichi
 */

@Getter
@Setter
public class RateLimiterPolicyBean {
    String id;
    RateLimiterPolicy.Type type;
    int replenishRate;
    int burstCapacity;
    int timeCount;
    TimeUnit timeUnit;

    public RateLimiterPolicyBean(String id, String type, int replenishRate, int burstCapacity, int timeCount, String timeUnit) {
        this.id = id;
        this.type = RateLimiterPolicy.Type.valueOf(type);
        this.replenishRate = replenishRate;
        this.burstCapacity = burstCapacity;
        this.timeCount = timeCount;
        this.timeUnit = TimeUnit.valueOf(timeUnit);
    }
}
