package com.github.dc.lock.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *     锁注解。
 *     根据name + businessKey属性为锁的唯一标识，持有该唯一标识对应的锁时，可以执行该方法，
 *     否则需等待其它线程释放锁才能执行注释的方法。
 *     不建议标注在耗时很大的方法上，导致锁开销更大，尽量在核心方法处标注使用。
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/8/1 17:02
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Lock {
    /**
     * 锁名
     * @return
     */
    String name() default "";
    /**
     * 该锁对应的业务主键.支持SpEL
     * @return
     */
    String businessKey() default "";
    /**
     * 锁类型
     * @return
     */
    Type type() default Type.REDIS_RWLOCK_LOCK;
    /**
     * redis锁类型的选项设置
     * @return
     */
    RedisLockOptions redisLockOptions() default @RedisLockOptions;


    enum Type {
        /**
         * 分布式锁：redis读写锁-trylock方式
         */
        REDIS_RWLOCK_TRYLOCK,
        /**
         * 分布式锁：redis读写锁-lock方式
         */
        REDIS_RWLOCK_LOCK
    }


    @interface RedisLockOptions {
        /**
         * 尝试加锁等待时间。
         * 如果 ’标注方法执行耗时 > 尝试加锁等待时间‘，则多线程同时执行该方法时，只有第一个拿到锁的线程能执行通过，
         * 其他由于直到规定等待时间后都没拿到锁而无法执行该方法。
         * 必须设置‘尝试加锁等待时间 > 标注方法执行耗时’，保证多线程情况下都能拿到锁执行方法。
         * 等待数据不宜过长，导致线程阻塞占用资源未被释放，有OOM风险。
         * @return
         */
        long waitTime() default 40;
        /**
         * 允许线程持有锁时间，若到规定设置时间，即使方法还未执行结束，因超时，锁自动释放，避免死锁。
         * 必须设置‘持有锁时间 > 方法执行耗时’，避免锁被提前释放，导致多个线程同时执行方法
         * @return
         */
        long leaseTime() default 10;
        /**
         * 上述时间单位，默认秒
         * @return
         */
        TimeUnit unit() default TimeUnit.SECONDS;
    }
}
