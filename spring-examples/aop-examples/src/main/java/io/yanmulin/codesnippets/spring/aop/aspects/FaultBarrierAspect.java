package io.yanmulin.codesnippets.spring.aop.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class FaultBarrierAspect {
    @Around("execution(public * execute(..))")
    Object handleException(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (RuntimeException exception) {
            System.out.println("unrecoverable unchecked exception " + exception);
        } catch (Throwable exception) {
            System.out.println("unexpected checked exception " + exception);
        } finally {
            System.out.println("cleaning up");
        }
        return null;
    }
}
