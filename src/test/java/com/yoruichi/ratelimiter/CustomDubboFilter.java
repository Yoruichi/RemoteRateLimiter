package com.yoruichi.ratelimiter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.yoruichi.ratelimiter.interceptor.RateLimiterDubboFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Yoruichi
 * @Date: 2019/2/13 11:45 AM
 */
@Slf4j
@Activate(group = { Constants.PROVIDER }, value = "MyRatelimiter")
public class CustomDubboFilter extends RateLimiterDubboFilter {

    @Override
    protected String getUserId(Invoker<?> invoker, Invocation invocation) {
        return ((TestBean) invocation.getArguments()[0]).getName();
    }
}
