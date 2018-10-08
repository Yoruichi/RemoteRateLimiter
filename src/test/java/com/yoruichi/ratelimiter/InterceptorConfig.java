package com.yoruichi.ratelimiter;

import com.yoruichi.ratelimiter.interceptor.RateLimiterInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @Author: Yoruichi
 * @Date: 2018/10/8 3:02 PM
 */

@Component
public class InterceptorConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private RateLimiterInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/**");
    }

}
