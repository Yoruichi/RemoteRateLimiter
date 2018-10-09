package com.yoruichi.ratelimiter;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RatelimiterApplicationTests {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    /**
     * 限流10秒1次
     * 按照最后允许的请求刷新更新时间
     * 每隔6秒发一次请求
     * 应该是交替成功和失败
     *
     * @throws Throwable
     */
    @Test
    public void test1() throws Throwable {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/test/limiter").accept(MediaType.APPLICATION_JSON))
                .andDo(r -> Assert.assertTrue("success".equalsIgnoreCase(r.getResponse().getContentAsString())))
                .andReturn();
        log.info(result.getResponse().getContentAsString());
        Thread.sleep(6000);
        result = mockMvc.perform(MockMvcRequestBuilders.get("/test/limiter").accept(MediaType.APPLICATION_JSON))
                .andDo(r -> Assert.assertTrue(!"success".equalsIgnoreCase(r.getResponse().getContentAsString())))
                .andReturn();
        log.info(result.getResponse().getContentAsString());
        Thread.sleep(6000);
        result = mockMvc.perform(MockMvcRequestBuilders.get("/test/limiter").accept(MediaType.APPLICATION_JSON))
                .andDo(r -> Assert.assertTrue("success".equalsIgnoreCase(r.getResponse().getContentAsString())))
                .andReturn();
        log.info(result.getResponse().getContentAsString());
        Thread.sleep(6000);
        result = mockMvc.perform(MockMvcRequestBuilders.get("/test/limiter").accept(MediaType.APPLICATION_JSON))
                .andDo(r -> Assert.assertTrue(!"success".equalsIgnoreCase(r.getResponse().getContentAsString())))
                .andReturn();
        log.info(result.getResponse().getContentAsString());
        Thread.sleep(6000);
        result = mockMvc.perform(MockMvcRequestBuilders.get("/test/limiter").accept(MediaType.APPLICATION_JSON))
                .andDo(r -> Assert.assertTrue("success".equalsIgnoreCase(r.getResponse().getContentAsString())))
                .andReturn();
        log.info(result.getResponse().getContentAsString());
    }

    /**
     * 限流10秒1次
     * 按照最后一次请求刷新更新时间
     * 每隔6秒发一次请求
     * 只有第一次是成功的
     * 因为每两次请求之间的间隔不到10秒
     *
     * @throws Throwable
     */
    @Test
    public void test5() throws Throwable {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/test/limiter5").accept(MediaType.APPLICATION_JSON))
                .andDo(r -> Assert.assertTrue("success".equalsIgnoreCase(r.getResponse().getContentAsString())))
                .andReturn();
        log.info(result.getResponse().getContentAsString());
        Thread.sleep(6000);
        result = mockMvc.perform(MockMvcRequestBuilders.get("/test/limiter5").accept(MediaType.APPLICATION_JSON))
                .andDo(r -> Assert.assertTrue(!"success".equalsIgnoreCase(r.getResponse().getContentAsString())))
                .andReturn();
        log.info(result.getResponse().getContentAsString());
        Thread.sleep(6000);
        result = mockMvc.perform(MockMvcRequestBuilders.get("/test/limiter5").accept(MediaType.APPLICATION_JSON))
                .andDo(r -> Assert.assertTrue(!"success".equalsIgnoreCase(r.getResponse().getContentAsString())))
                .andReturn();
        log.info(result.getResponse().getContentAsString());
        Thread.sleep(6000);
        result = mockMvc.perform(MockMvcRequestBuilders.get("/test/limiter5").accept(MediaType.APPLICATION_JSON))
                .andDo(r -> Assert.assertTrue(!"success".equalsIgnoreCase(r.getResponse().getContentAsString())))
                .andReturn();
        log.info(result.getResponse().getContentAsString());
        Thread.sleep(6000);
        result = mockMvc.perform(MockMvcRequestBuilders.get("/test/limiter5").accept(MediaType.APPLICATION_JSON))
                .andDo(r -> Assert.assertTrue(!"success".equalsIgnoreCase(r.getResponse().getContentAsString())))
                .andReturn();
        log.info(result.getResponse().getContentAsString());
    }

    @Test
    public void test3() throws Throwable {
        // 初始化并发请求数组
        AtomicInteger count = new AtomicInteger();
        TestRunnable[] trs = new TestRunnable[4];
        generateTestRunnableForTest3(count, trs);
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);

        // 初始化计数器
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/test/limiter3").accept(MediaType.APPLICATION_JSON))
                .andDo(r -> Assert.assertTrue("success".equalsIgnoreCase(r.getResponse().getContentAsString())))
                .andReturn();
        log.info(result.getResponse().getContentAsString());
        // 等待2秒 模拟在一个时间窗口的最末端并发4个请求
        Thread.sleep(2000);
        mttr.runTestRunnables();
        // 在最后一秒内应该只有两个请求成功
        log.info("success in last second count {}", count.get());
        Assert.assertTrue(count.get() == 2);
        // 无关并发的先后顺序 本次必然失败
        result = mockMvc.perform(MockMvcRequestBuilders.get("/test/limiter3").accept(MediaType.APPLICATION_JSON))
                .andDo(r -> Assert.assertTrue(!"success".equalsIgnoreCase(r.getResponse().getContentAsString())))
                .andReturn();
        log.info(result.getResponse().getContentAsString());
        // 失败的请求不会重置时间窗口 等待3秒 仍然失败
        Thread.sleep(3000);
        result = mockMvc.perform(MockMvcRequestBuilders.get("/test/limiter3").accept(MediaType.APPLICATION_JSON))
                .andDo(r -> Assert.assertTrue(!"success".equalsIgnoreCase(r.getResponse().getContentAsString())))
                .andReturn();
        log.info(result.getResponse().getContentAsString());
        // 重置计数
        count.set(0);
        trs = new TestRunnable[4];
        generateTestRunnableForTest3(count, trs);
        mttr = new MultiThreadedTestRunner(trs);
        // 继续等待3秒 此时距离最后一次成功已过5秒钟 应该已经补充了1个新的令牌
        Thread.sleep(3000);
        mttr.runTestRunnables();
        log.info("success in last second count {}", count.get());
        Assert.assertTrue(count.get() >= 1);
    }

    private void generateTestRunnableForTest3(AtomicInteger count, TestRunnable[] trs) throws Exception {
        for (int i = 0; i < trs.length; i++) {
            trs[i] = new TestRunnable() {
                @Override
                public void runTest() throws Throwable {
                    mockMvc.perform(MockMvcRequestBuilders.get("/test/limiter3").accept(MediaType.APPLICATION_JSON))
                            .andDo(r -> {
                                if ("success".equalsIgnoreCase(r.getResponse().getContentAsString())) {
                                    count.incrementAndGet();
                                }
                            });
                }
            };
        }
    }

    /**
     * 两个限流策略 需要注意策略配置的顺序
     * 1. 每分钟30次
     * 2. 每秒3次
     *
     * @throws Throwable
     */
    @Test
    public void test2And4() throws Throwable {
        AtomicInteger count = new AtomicInteger();
        int runnerSize = 5;
        TestRunnable[] trs = new TestRunnable[runnerSize];
        for (int i = 0; i < trs.length; i++) {
            trs[i] = new TestRunnable() {
                @Override
                public void runTest() throws Throwable {
                    for (int j = 1; j <= 20; j++) {
                        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/test/limiter2-4").accept(MediaType.APPLICATION_JSON))
                                .andDo(r -> {
                                    if ("success".equalsIgnoreCase(r.getResponse().getContentAsString())) {
                                        count.incrementAndGet();
                                    }
                                }).andReturn();
                        log.info(result.getResponse().getContentAsString());
                        Assert.assertTrue(count.get() <= 3 * j);
                        Thread.sleep(1000);
                    }
                }
            };
        }
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);
        mttr.runTestRunnables();
        log.info("{}", count.get());
        Assert.assertTrue(count.get() == 30);

    }


}
