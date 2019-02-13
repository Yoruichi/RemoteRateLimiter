package com.yoruichi.ratelimiter;

import com.alibaba.dubbo.config.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Yoruichi
 * @Date: 2018/12/14 6:49 PM
 */
@Slf4j
@Configuration
public class DubboConfig {

    @Value("${cerberus.dubbo.zk.addr:10.60.81.235:2181,10.60.81.238:2181,10.60.81.230:2181}")
    private String dubboZookeeperAddress;

    @Value("${cerberus.dubbo.application.name:TestFilter}")
    private String applicationName;

    @Value("${cerberus.dubbo.logger.name:slf4j}")
    private String loggerName;

    @Value("${cerberus.dubbo.interface.version:1.0.0.local}")
    private String serviceVersion;

    @Value("${cerberus.dubbo.provider.port:28080}")
    private Integer servicePort;

    @Value("${cerberus.dubbo.register.file:dubbo/registry.properties}")
    private String registryFile;

    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(applicationName);
        applicationConfig.setLogger(loggerName);
        applicationConfig.setVersion(serviceVersion);
        return applicationConfig;
    }

    @Bean
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(dubboZookeeperAddress);
        registryConfig.setFile(registryFile);
        registryConfig.setProtocol("zookeeper");
        return registryConfig;
    }

    @Bean
    public ProtocolConfig protocolConfig() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setName("dubbo");
        protocolConfig.setPort(servicePort);
        return protocolConfig;
    }

    @Bean
    public ProviderConfig providerConfig() {
        ProviderConfig providerConfig = new ProviderConfig();
        providerConfig.setFilter("ratelimiter");
        return providerConfig;
    }

    @Bean
    public ConsumerConfig consumerConfig() {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        return consumerConfig;
    }

}
