package io.yanmulin.codesnippets.spring.aop.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.Ordered;

@Aspect
public class AspectA implements Ordered {
    @Override
    public int getOrder() {
        return 1;
    }

    @Pointcut("execution(public void execute(..))")
    public void pointcut() {}

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        System.out.println("- AspectA.before(): " + joinPoint.toShortString());
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("- AspectA.around(): " + joinPoint.toShortString());
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            throw ex;
        }
    }

    @After("pointcut()")
    public void after(JoinPoint joinPoint) {
        System.out.println("- AspectA.after(): " + joinPoint.toShortString());
    }

    @AfterThrowing(value = "pointcut()", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Exception exception) {
        System.out.println("- AspectA.afterThrowing(): " + joinPoint.toShortString()
                + ", throwing=" + exception);
    }

    @AfterReturning(value = "pointcut()", returning = "retVal")
    public void afterReturning(JoinPoint joinPoint, Object retVal) {
        System.out.println("- AspectA.afterReturning(): " + joinPoint.toShortString()
                + ", ret=" + retVal);
    }

}
