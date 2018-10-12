package com.yoruichi.ratelimiter.service;

import java.util.concurrent.TimeUnit;

/**
 * @author yoruichi
 */

public interface RateLimiter {
    /**
     * Token bucket
     *
     * @param id            tokens consistent id
     * @param replenishRate How many requests do you want to replenish in {@param timeCount} * {@param timeUnit}
     * @param burstCapacity How much bursting do you want to allow in {@param timeCount} * {@param timeUnit}?
     *                      0 means to forbidden any request.
     * @param timeCount
     * @param timeUnit
     * @param rateType  1 means to set last refresh time value of first request time or last replenish time.
     *                  2 means to set last refresh time value of last allowed request time.
     *                  3 means to set last refresh time value of last request time.
     * @param requested How many tokens for this request.
     *
     * @return
     */
    boolean isAllowed(String id, int replenishRate, int burstCapacity, int timeCount, TimeUnit timeUnit, int rateType, int requested);
}
