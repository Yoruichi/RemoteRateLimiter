package com.yoruichi.ratelimiter;

import com.yoruichi.ratelimiter.annotation.RateLimiterPolicies;
import com.yoruichi.ratelimiter.annotation.RateLimiterPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Yoruichi
 * @Date: 2018/10/8 2:59 PM
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @RateLimiterPolicies(names = {"policyOne"})
    @GetMapping("/limiter")
    public String test1() throws InterruptedException {
        Thread.sleep(1000);
        return "success";
    }

    @RateLimiterPolicy(
            value = "5",
            type = RateLimiterPolicy.Type.IP,
            replenishRate = 1,
            burstCapacity = 1,
            timeCount = 10,
            refreshType = RateLimiterPolicy.RefreshType.LAST_REQUEST
    )
    @GetMapping("/limiter5")
    public String test5() throws InterruptedException {
        Thread.sleep(1000);
        return "success";
    }

    @RateLimiterPolicies(names = {"policyFour", "policyTwo"})
    @GetMapping("/limiter2-4")
    public String test2And4() throws InterruptedException {
        Thread.sleep(1000);
        return "success";
    }

    @RateLimiterPolicies(names = {"policyThr"})
    @GetMapping("/limiter3")
    public String test3() throws InterruptedException {
        Thread.sleep(1000);
        return "success";
    }
}
