package io.yanmulin.codesnippets.spring.aop.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.StopWatch;

@Aspect
public class ExecuteTimeRecordAspect {
    @Around("execution(public * execute(..))")
    Object handleException(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            throw throwable;
        } finally {
            System.out.println("elapsed " + stopWatch.getTotalTimeMillis() + "ms");
        }
    }
}
