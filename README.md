#RemoteRateLimiter

### RateLimiter接口
```$xslt
/**
     * Token bucket
     *
     * @param id            tokens consistent id
     * @param replenishRate How many requests do you want to replenish in {@param timeCount} * {@param timeUnit}
     * @param burstCapacity How much bursting do you want to allow in {@param timeCount} * {@param timeUnit}?
     *                      0 means to forbidden any request.
     * @param timeCount
     * @param timeUnit
     * @param rateType  1 means to set last refresh time value of first request time or last replenish time.
     *                  2 means to set last refresh time value of last allowed request time.
     *                  3 means to set last refresh time value of last request time.
     * @return
     */
    boolean isAllowed(String id, int replenishRate, int burstCapacity, int timeCount, TimeUnit timeUnit, int rateType);
```

当设置 `timeCount` 为 `1` 设置 `timeUnit` 为 `SECONDS` 设置 `rateType` 为 `3` 的时候，该接口应该提供基于令牌桶算法的实现。

当设置 `timeCount` 为 `1` 设置 `timeUnit` 为 `SECONDS` 设置 `rateType` 为 `2` 的时候，该接口应该提供基于令牌桶算法变种的实现。

当设置 `rateType` 为 `1` 的时候，该接口应该提供基础的时间窗口限流计数的实现。   
    
### RedisRateLimiter的实现

为每个限流策略在Redis里生成一对Key: 

```
request_rate_limiter.{@param id}.tokens
request_rate_limiter.{@param id}.timestamp
```

分别存放着限流策略对应的剩余可用tokens的数量和最后更新时间。

####  Lua 脚本

令牌桶算法的变种实现。每次请求去计算当前时间和最后更新时间之间的差值，根据时间差来计算需要补充的令牌数量。

### RateLimiterInterceptor的实现

通过获取注解 `@RateLimiterPolicy` 和 `@RateLimiterPolicies` 来生成限流策略。再通过调用 `RedisRateLimiter` 来判断是否限流当前请求。

#### @RateLimiterPolicy
限流策略

属性 `value` 和 `type` 共同决定了限流策略的 `id`

相同功能的Bean为 `RateLimiterPolicyBean`

#### @RateLimiterPolicies
限流策略组

属性 `names` 会依赖Spring上下文来获取对应名称的限流策略Bean

属性 `policies` 可以直接写入一个或多个策略

需要注意组内策略的先后顺序，原则上需要把最可能生效(限流成功)的策略放在前面，以避免浪费策略的令牌数。

### 使用示例

#### 1. 实现一个Controller
提供一个REST接口，返回字符 `'success'`

给接口添加 `@RateLimiterPolicy` 声明限流策略为每10秒限流一次请求，更新时间戳类型为最后一次通过的请求。
```java
@RestController
@RequestMapping("/test")
public class TestController {

    @RateLimiterPolicy(
            value = "one",
            type = RateLimiterPolicy.Type.IP,
            replenishRate = 1,
            burstCapacity = 1,
            timeCount = 10,
            refreshType = RateLimiterPolicy.RefreshType.LAST_ALLOWED_REQUEST
    )
    @GetMapping("/limiter")
    public String test1() throws InterruptedException {
        Thread.sleep(1000);
        return "success";
    }
}
```
#### 2. 测试代码

一共发送5次请求，每隔6秒发送一次。根据限流策略预期结果为请求交替被拒绝。

```$xslt
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
```

#### 3. 注册策略Bean方式

依赖Spring 声明策略Bean 每10秒限流一次请求
```java
@Component
public class PoliciesConfig {
    /**
         * Policy One
         * Most 1 request in 10 seconds
         * @return
         */
        @Bean(name = "policyOne")
        public RateLimiterPolicyBean policyBeanOne() {
            return new RateLimiterPolicyBean("one", "IP", 1, 1, 10, "SECONDS", 2);
        }
}
```
接口使用 `names` 来注入策略Bean
```$xslt
    @RateLimiterPolicies(names = {"policyOne"})
    @GetMapping("/limiter")
    public String test1() throws InterruptedException {
        Thread.sleep(1000);
        return "success";
    }
```

#### 4. 更多示例
参考 
<a href="https://github.com/Yoruichi/RemoteRateLimiter/blob/master/src/test/java/com/yoruichi/ratelimiter/RatelimiterApplicationTests.java">
RateLimiterApplicationTests.java
</a>

### 依赖
spring-boot-starter-date-redis

spring-boot-starter-web

guava

lombok

net.sourceforge.groboutils
