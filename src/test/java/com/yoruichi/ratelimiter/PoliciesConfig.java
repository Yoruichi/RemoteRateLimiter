package com.yoruichi.ratelimiter;

import com.yoruichi.ratelimiter.bean.RateLimiterPolicyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

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
        return new RateLimiterPolicyBean("one", "IP", 1, 1, 10, "SECONDS");
    }

    /**
     * Policy Two
     * Expect 3 requests and allowed most 10 requests in 1 minute (60 seconds)
     * @return
     */
    @Bean(name = "policyTwo")
    public RateLimiterPolicyBean policyBeanTwo() {
        return new RateLimiterPolicyBean("two", "IP", 3, 10, 1, "MINUTES");
    }

    /**
     * Policy Two
     * Expect 3 requests per second and allowed most 10 requests in 1 minute (60 seconds)
     * @return
     */
    @Bean(name = "policyFour")
    public RateLimiterPolicyBean policyBeanFour() {
        return new RateLimiterPolicyBean("two", "IP", 3, 10, 1, "MINUTES");
    }

    /**
     * Policy Three
     * Expect 1 request per second and allowed most 3 requests in 5 seconds
     * @return
     */
    @Bean(name = "policyThr")
    public RateLimiterPolicyBean policyBeanThr() {
        return new RateLimiterPolicyBean("two", "IP", 1, 3, 5, "SECONDS");
    }
}
