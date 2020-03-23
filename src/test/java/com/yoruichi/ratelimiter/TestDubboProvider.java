package com.yoruichi.ratelimiter;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;

/**
 * @Author: Yoruichi
 * @Date: 2019/2/12 5:02 PM
 */
@Slf4j
@Service(version = "1.0.0", parameters = { "sayHello.rateLimiterPolicies", "policyThr" }, filter = "MyRatelimiter")
public class TestDubboProvider implements TestService {

    @Override
    public String sayHello(TestBean bean) {
        return "Hello, " + bean.getName();
    }

}
