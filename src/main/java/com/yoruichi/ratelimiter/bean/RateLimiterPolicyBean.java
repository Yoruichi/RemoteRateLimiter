package com.yoruichi.ratelimiter.bean;

import com.yoruichi.ratelimiter.annotation.RateLimiterPolicy;
import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * @author yoruichi
 */

@Data
public class RateLimiterPolicyBean {
    String id;
    RateLimiterPolicy.Type type;
    int replenishRate;
    int burstCapacity;
    int timeCount;
    TimeUnit timeUnit;
    RateLimiterPolicy.RefreshType refreshType;
    int requested;
}
