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

    RefreshType refreshType() default RefreshType.LAST_ALLOWED_REQUEST;

    int requested() default 1;

    enum RefreshType {
        /**
         * Set last refresh time value of first request time or last replenish time.
         */
        FIRST_REQUEST(1),
        /**
         * Set last refresh time value of last allowed request time.
         */
        LAST_ALLOWED_REQUEST(2),
        /**
         * Set last refresh time value of last request time.
         */
        LAST_REQUEST(3);

        int value;

        RefreshType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        /**
         * default LAST_ALLOWED_REQUEST
         */
        public static RefreshType valueOf(int value) {
            RefreshType[] values = RefreshType.values();
            for (int i = 0; i < values.length; i++) {
                if (values[i].getValue() == value) {
                    return values[i];
                }
            }
            return LAST_ALLOWED_REQUEST;
        }
    }

    enum Type {
        /* 统一控制 */
        GENERAL,
        /* 按IP进行控制 */
        IP,
        /* 按API-KEY控制 */
        USER
    }

}
