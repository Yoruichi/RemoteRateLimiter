package com.yoruichi.ratelimiter;

import com.yoruichi.ratelimiter.annotation.RateLimiterPolicy;
import com.yoruichi.ratelimiter.bean.RateLimiterPolicyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Yoruichi
 * @Date: 2018/10/8 2:54 PM
 */
@Component
public class PoliciesConfig {
    /**
     * Policy One
     * Most 1 request in 10 seconds
     * @return
     */
    @Bean(name = "policyOne")
    public RateLimiterPolicyBean policyBeanOne() {
        return new RateLimiterPolicyBean("one", RateLimiterPolicy.Type.IP, 1, 1, 10, TimeUnit.SECONDS, RateLimiterPolicy.RefreshType.FIRST_REQUEST, 2);
    }

    /**
     * Policy Two
     * Expect 30 requests and allowed most 30 requests in 1 minute (60 seconds)
     * @return
     */
    @Bean(name = "policyTwo")
    public RateLimiterPolicyBean policyBeanTwo() {
        return new RateLimiterPolicyBean("two", RateLimiterPolicy.Type.IP, 30, 30, 1, TimeUnit.MINUTES, RateLimiterPolicy.RefreshType.FIRST_REQUEST,1);
    }

    /**
     * Policy Four
     * Expect 1 requests and allowed most 3 requests per second
     * @return
     */
    @Bean(name = "policyFour")
    public RateLimiterPolicyBean policyBeanFour() {
        return new RateLimiterPolicyBean("Four", RateLimiterPolicy.Type.IP, 1, 3, 1, TimeUnit.SECONDS, RateLimiterPolicy.RefreshType.FIRST_REQUEST,1);
    }

    /**
     * Policy Three
     * Expect 1 request per second and allowed most 3 requests in 5 seconds
     * @return
     */
    @Bean(name = "policyThr")
    public RateLimiterPolicyBean policyBeanThr() {
        return new RateLimiterPolicyBean("two", RateLimiterPolicy.Type.USER, 1, 3, 5, TimeUnit.SECONDS, RateLimiterPolicy.RefreshType.FIRST_REQUEST,2);
    }
}
