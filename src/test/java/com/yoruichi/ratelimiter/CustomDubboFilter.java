package com.yoruichi.ratelimiter;

import com.yoruichi.ratelimiter.interceptor.RateLimiterDubboFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

/**
 * @Author: Yoruichi
 * @Date: 2019/2/13 11:45 AM
 */
@Slf4j
@Activate(group = PROVIDER, value = "MyRatelimiter")
public class CustomDubboFilter extends RateLimiterDubboFilter {

    @Override
    protected String getUserId(Invoker<?> invoker, Invocation invocation) {
        return ((TestBean) invocation.getArguments()[0]).getName();
    }
}
