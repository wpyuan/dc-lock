package com.github.dc.lock.service;

import com.github.dc.lock.annotation.Lock;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * <p>
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/8/2 10:48
 */
public interface ILockAspect {
    /**
     * 根据锁机制执行目标方法
     * @param proceedingJoinPoint
     * @param lock
     * @param lockName
     * @return
     * @throws Throwable
     */
    Object run(ProceedingJoinPoint proceedingJoinPoint, Lock lock, String lockName) throws Throwable;
}
