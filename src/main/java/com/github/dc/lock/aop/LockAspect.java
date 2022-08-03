package com.github.dc.lock.aop;

import com.github.dc.lock.annotation.Lock;
import com.github.dc.lock.service.impl.RedisLockAspect;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * <p>
 *     锁注解切面
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/8/1 17:37
 */
@Aspect
@Component
@Slf4j
public class LockAspect {

    @Autowired
    private RedisLockAspect redisLockAspect;

    /**
     * 前置处理
     *
     * @param proceedingJoinPoint 切点
     * @param lock 锁注解
     */
    @Around(value = "@annotation(lock)")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint, Lock lock) throws Throwable {
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(lock.businessKey());
        EvaluationContext context = new StandardEvaluationContext();
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        Object[] arguments = proceedingJoinPoint.getArgs();
        String[] paramNames = u.getParameterNames(method);
        for (int i = 0; i < arguments.length; i++) {
            context.setVariable(paramNames[i], arguments[i]);
        }

        String businessKey = expression.getValue(context, String.class);
        String lockName = "lock:" + lock.name() + ":"+ businessKey;

        return redisLockAspect.run(proceedingJoinPoint, lock, lockName);
    }

}
