package com.yoruichi.ratelimiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RateLimiterPolicy {
    String value() default "root";

    Type type() default Type.GENERAL;

    int replenishRate();

    int burstCapacity();

    int timeCount() default 1;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    enum Type {
        /* 统一控制 */
        GENERAL,
        /* 按IP进行控制 */
        IP,
        /* 按API-KEY控制 */
        USER
    }

}
