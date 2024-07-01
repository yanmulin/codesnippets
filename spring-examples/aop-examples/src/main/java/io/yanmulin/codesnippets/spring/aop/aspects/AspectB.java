package io.yanmulin.codesnippets.spring.aop.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;

@Aspect
public class AspectB implements Ordered {
    @Override
    public int getOrder() {
        return 2;
    }

    @Pointcut("execution(public void execute(..))")
    public void pointcut() {}

    @Before("pointcut()")
    public void firstBefore(JoinPoint joinPoint) {
        System.out.println("- AspectB.firstBefore(): " + joinPoint.toShortString());
    }

    @Before("pointcut()")
    public void secondBefore(JoinPoint joinPoint) {
        System.out.println("- AspectB.secondBefore(): " + joinPoint.toShortString());
    }
}
