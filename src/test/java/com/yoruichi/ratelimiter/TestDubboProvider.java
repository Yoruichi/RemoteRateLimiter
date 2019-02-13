package com.yoruichi.ratelimiter;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.support.Parameter;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Yoruichi
 * @Date: 2019/2/12 5:02 PM
 */
@Slf4j
@Service(version = "1.0.0", parameters = { "sayHello.rateLimiterPolicies", "policyThr" })
public class TestDubboProvider implements TestService {

    @Override
    @Parameter(key = "rateLimiterPolicies")
    public String sayHello(TestBean bean) {
        return "Hello, " + bean.getName();
    }

}
