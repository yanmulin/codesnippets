package io.yanmulin.codesnippets.spring.aop.target;

import lombok.Setter;
import org.springframework.aop.framework.AopContext;

public class NestableInvocationTarget implements IExecute {

    @Setter
    boolean fromProxy = false;

    @Override
    public void execute(String message, RuntimeException exception) {
        if (fromProxy) {
            ((NestableInvocationTarget) AopContext.currentProxy()).execute(message);
        } else {
            execute(message);
        }
        if (exception != null) throw exception;
    }

    public void execute(String message) {
        System.out.println("NestableInvocationTarget.execute(message=\"" + message + "\")");
    }
}
