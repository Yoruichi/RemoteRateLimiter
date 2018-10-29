package com.yoruichi.ratelimiter.interceptor;

import com.yoruichi.ratelimiter.annotation.RateLimiterPolicies;
import com.yoruichi.ratelimiter.annotation.RateLimiterPolicy;
import com.yoruichi.ratelimiter.bean.RateLimiterPolicyBean;
import com.yoruichi.ratelimiter.service.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author yoruichi
 */

@Slf4j
@Component
public class RateLimiterInterceptor extends HandlerInterceptorAdapter implements ApplicationContextAware {

    private static final String GENERAL_RATE_LIMIT_CODE = "__general";

    @Autowired
    private RateLimiter rateLimiter;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return false;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        if (method.isAnnotationPresent(RateLimiterPolicy.class)) {
            RateLimiterPolicy rateLimiterPolicy = method.getAnnotation(RateLimiterPolicy.class);
            if (!isAllowedForRateLimiterPolicy(rateLimiterPolicy, request)) {
                log.debug("Denied by policy {}.", rateLimiterPolicy.value());
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                return false;
            }
        }
        if (method.isAnnotationPresent(RateLimiterPolicies.class)) {
            RateLimiterPolicies rateLimiterPoliciesAnno = method.getAnnotation(RateLimiterPolicies.class);
            String[] names = rateLimiterPoliciesAnno.names();
            for (int i = 0; i < names.length; i++) {
                try {
                    RateLimiterPolicyBean policyBean = apc.getBean(names[i], RateLimiterPolicyBean.class);
                    if (!isAllowedForRateLimiterPolicy(policyBean, request)) {
                        log.debug("Denied by policy {}.", names[i]);
                        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                        return false;
                    }
                } catch (BeansException e) {
                    log.warn("Failed to get RateLimiterPolicyBean. Caused by:", e);
                }

            }
            RateLimiterPolicy[] rateLimiterPolicies = rateLimiterPoliciesAnno.policies();
            for (int i = 0; i < rateLimiterPolicies.length; i++) {
                if (!isAllowedForRateLimiterPolicy(rateLimiterPolicies[i], request)) {
                    log.debug("Denied by policy {}.", rateLimiterPolicies[i].value());
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isAllowedForRateLimiterPolicy(RateLimiterPolicy policy, HttpServletRequest request) {
        String rateLimitId = policy.value();
        RateLimiterPolicy.Type type = policy.type();
        String rateLimitCode = getRateLimitCode(request, type);
        String id = rateLimitId + rateLimitCode;
        int replenishRate = policy.replenishRate();
        int burstCapacity = policy.burstCapacity();
        int timeCount = policy.timeCount();
        TimeUnit timeUnit = policy.timeUnit();
        int rateType = policy.refreshType().getValue();
        int requested = policy.requested();

        return this.rateLimiter.isAllowed(id, replenishRate, burstCapacity, timeCount, timeUnit, rateType, requested);
    }

    private boolean isAllowedForRateLimiterPolicy(RateLimiterPolicyBean policy, HttpServletRequest request) {
        String rateLimitId = policy.getId();
        RateLimiterPolicy.Type type = policy.getType();
        String rateLimitCode;
        rateLimitCode = getRateLimitCode(request, type);
        String id = rateLimitId + rateLimitCode;
        int replenishRate = policy.getReplenishRate();
        int burstCapacity = policy.getBurstCapacity();
        int timeCount = policy.getTimeCount();
        TimeUnit timeUnit = policy.getTimeUnit();
        int rateType = policy.getRefreshType().getValue();
        int requested = policy.getRequested();

        return this.rateLimiter.isAllowed(id, replenishRate, burstCapacity, timeCount, timeUnit, rateType, requested);
    }

    private String getRateLimitCode(HttpServletRequest request, RateLimiterPolicy.Type type) {
        String rateLimitCode;
        switch (type) {
        case GENERAL:
            rateLimitCode = GENERAL_RATE_LIMIT_CODE;
            break;
        case IP:
            rateLimitCode = getIPAddress(request);
            break;
        case USER:
            rateLimitCode = getUserId(request);
            break;
        default:
            rateLimitCode = getIPAddress(request);
        }
        return rateLimitCode;
    }

    protected String getIPAddress(HttpServletRequest request) {
        String ip = null;

        //X-Forwarded-For：Squid 服务代理
        String ipAddresses = request.getHeader("X-Forwarded-For");

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //Proxy-Client-IP：apache 服务代理
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //X-Real-IP：nginx服务代理
            ipAddresses = request.getHeader("X-Real-IP");
        }

        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length() != 0) {
            ip = ipAddresses.split(",")[0];
        }

        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * You should rewrite this method if you choose rate limiter policy type USER.
     *
     * @param request
     * @return user id
     */
    protected String getUserId(HttpServletRequest request) {
        return "user_id";
    }

    private ApplicationContext apc;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.apc = applicationContext;

    }
}
