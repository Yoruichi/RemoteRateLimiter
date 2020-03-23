package com.yoruichi.ratelimiter.interceptor;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.yoruichi.ratelimiter.annotation.RateLimiterPolicy;
import com.yoruichi.ratelimiter.bean.ApcUtil;
import com.yoruichi.ratelimiter.bean.RateLimiterPolicyBean;
import com.yoruichi.ratelimiter.service.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.springframework.beans.BeansException;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

/**
 * @Author: Yoruichi
 * @Date: 2019/2/12 3:12 PM
 * <p>
 * http://dubbo.apache.org/en-us/docs/dev/impls/filter.html
 */
@Slf4j
@Activate(group = PROVIDER, value = "ratelimiter")
public class RateLimiterDubboFilter implements Filter {
    private static final String GENERAL_RATE_LIMIT_CODE = "__general";
    private static final String RATE_LIMIT_ATTACHMENT_KEY = "rateLimiterPolicies";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String methodName = invocation.getMethodName();
        String policies = invoker.getUrl().getMethodParameter(methodName, RATE_LIMIT_ATTACHMENT_KEY, "");
        if (!Strings.isNullOrEmpty(policies)) {
            List<String> names = Splitter.on(",").splitToList(policies);
            for (String name : names) {
                try {
                    RateLimiterPolicyBean policyBean = ApcUtil.apc.getBean(name, RateLimiterPolicyBean.class);
                    if (!isAllowedForRateLimiterPolicy(policyBean, invoker, invocation)) {
                        log.debug("Denied by policy {}.", names);
                        throw new RpcException(RpcException.LIMIT_EXCEEDED_EXCEPTION,
                                "Waiting concurrent invoke timeout in provider-side for service:  " + invoker.getInterface().getName() + ", method: "
                                        + invocation.getMethodName() + ", policy: " + names);
                    }
                } catch (BeansException e) {
                    log.warn("Failed to get RateLimiterPolicyBean. Caused by:", e);
                }

            }
        }
        Result result = invoker.invoke(invocation);
        return result;
    }

    private boolean isAllowedForRateLimiterPolicy(RateLimiterPolicyBean policy, Invoker<?> invoker, Invocation invocation) {
        String rateLimitId = policy.getId();
        RateLimiterPolicy.Type type = policy.getType();
        String rateLimitCode;
        rateLimitCode = getRateLimitCode(invoker, type, invocation);
        String id = rateLimitId + rateLimitCode;
        int replenishRate = policy.getReplenishRate();
        int burstCapacity = policy.getBurstCapacity();
        int timeCount = policy.getTimeCount();
        TimeUnit timeUnit = policy.getTimeUnit();
        int rateType = policy.getRefreshType().getValue();
        int requested = policy.getRequested();
        RateLimiter rateLimiter = ApcUtil.apc.getBean(RateLimiter.class);
        return rateLimiter.isAllowed(id, replenishRate, burstCapacity, timeCount, timeUnit, rateType, requested);
    }

    private String getRateLimitCode(Invoker<?> invoker, RateLimiterPolicy.Type type, Invocation invocation) {
        String rateLimitCode;
        switch (type) {
        case GENERAL:
            rateLimitCode = GENERAL_RATE_LIMIT_CODE;
            break;
        case IP:
            rateLimitCode = getIPAddress(invoker);
            break;
        case USER:
            rateLimitCode = getUserId(invoker, invocation);
            break;
        default:
            rateLimitCode = getIPAddress(invoker);
        }
        return rateLimitCode;
    }

    protected String getIPAddress(Invoker<?> invoker) {
        return invoker.getUrl().getIp();
    }

    /**
     * You should rewrite this method if you choose rate limiter policy type USER.
     *
     * @param invoker
     * @return user id
     */
    protected String getUserId(Invoker<?> invoker, Invocation invocation) {
        return "user_id";
    }

}
