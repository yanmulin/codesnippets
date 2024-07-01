package io.yanmulin.codesnippets.spring.aop.advices;

import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.StringJoiner;

public class TargetBeforeAdvice implements MethodBeforeAdvice {
    @Override
    public void before(Method method, Object[] args, Object target) {
        StringJoiner argsJoiner = new StringJoiner(",");
        Arrays.stream(args).forEach(arg -> argsJoiner.add(String.valueOf(arg)));
        String signature = String.format("%s.%s(%s)", target.getClass().getSimpleName(), method.getName(), argsJoiner);
        System.out.println("- TargetBeforeAdvice.before(): " + signature);
    }
}
