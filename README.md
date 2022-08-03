# dc-lock
springboot框架的锁封装，开箱即用

## 使用说明

### 在方法上添加注解`@Lock`

#### 1、严格的后台任务方法
##### 线程等待到拿到锁执行方法为止，才进行方法返回，适合严格的后台任务，不需要及时响应调用方

```java
    ...
    @Lock(name = "测试方法1", businessKey = "#id+#other")
    public void lock(Long id, String other) {
        // do something
    }
    ...
```

#### 2、需要及时响应的前端接口调用方法
##### 线程等待时间内拿不到锁，报错提示“资源被其他线程占用”，适合需要及时响应的前端接口调用，`waitTime`建议设置在前端报超时的合理时间范围内，提示前端用户“稍后再试”
```java
    ...
    @Lock(name = "测试方法2", businessKey = "#id+#other", 
            type = Lock.Type.REDIS_RWLOCK_TRYLOCK,
            redisLockOptions = @Lock.RedisLockOptions(waitTime = 20)
    )
    public void readAndWrite(Long id, String other) {
        // do something
    }
    ...
```

## 注意事项
#### 不能把锁加在耗时巨大的方法上，增加资源占用时长，不利于资源回收，且影响方法执行效率