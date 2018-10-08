package com.yoruichi.ratelimiter;

import com.yoruichi.ratelimiter.annotation.RateLimiterPolicies;
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

    @RateLimiterPolicies(names = {"policyTwo"})
    @GetMapping("/limiter2")
    public String test2() throws InterruptedException {
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
