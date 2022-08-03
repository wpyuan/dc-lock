package com.github.dc.lock.service.impl;

import com.github.dc.lock.annotation.Lock;
import com.github.dc.lock.service.ILockAspect;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 *     redis分布式锁辅助类
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/8/2 10:40
 */
@Component
@Slf4j
public class RedisLockAspect implements ILockAspect {

    @Autowired(required = false)
    private RedissonClient redissonClient;

    @Override
    public Object run(ProceedingJoinPoint proceedingJoinPoint, Lock lock, String lockName) throws Throwable {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(lockName);
        boolean tryLockResult = false;
        boolean tryLockError = false;
        long waitTime = lock.redisLockOptions().waitTime();
        long leaseTime = lock.redisLockOptions().leaseTime();
        TimeUnit unit = lock.redisLockOptions().unit();
        Object result = null;
        try {

            switch (lock.type()) {
                case REDIS_RWLOCK_LOCK:
                    readWriteLock.writeLock().lock();
                    tryLockResult = true;
                    break;
                case REDIS_RWLOCK_TRYLOCK:
                default:
                    try {
                        tryLockResult = readWriteLock.writeLock().tryLock(waitTime, leaseTime, unit);
                    } catch (InterruptedException e) {
                        tryLockError = true;
                        log.warn("尝试加锁失败，线程异常中断", e);
                    }
            }

            if (tryLockError) {
                throw new RuntimeException("该数据被其他用户编辑中，请稍后再试");
            }
            if (tryLockResult) {
                //成功
                result = proceedingJoinPoint.proceed();
            } else {
                // 申请锁失败，且超过等待时间
                if (log.isWarnEnabled()) {
                    log.warn("等待时间内申请锁失败，线程未执行方法。锁名：[{}]，等待时间：{}{}", lockName, waitTime, unit);
                }
                throw new RuntimeException("该数据被其他用户编辑中，请稍后再试");
            }
        } finally {
            if (tryLockResult && readWriteLock.writeLock().isLocked() && readWriteLock.writeLock().isHeldByCurrentThread()) {
                readWriteLock.writeLock().unlock();
            }
        }
        return result;
    }
}
