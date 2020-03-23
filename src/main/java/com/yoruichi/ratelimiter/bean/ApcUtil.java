package com.yoruichi.ratelimiter.bean;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @Author: Yoruichi
 * @Date: 2020/3/23 12:38 PM
 */
public class ApcUtil implements ApplicationContextAware {

    public static ApplicationContext apc;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.apc = applicationContext;
    }
}
