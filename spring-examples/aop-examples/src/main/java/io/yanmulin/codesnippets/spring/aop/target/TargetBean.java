package io.yanmulin.codesnippets.spring.aop.target;

public class TargetBean implements IExecute {
    @Override
    public void execute(String message, RuntimeException exception) {
        System.out.println("TargetBean.execute(message=\"" + message + "\", ex=" +  exception + ")");
        if (exception != null) throw exception;
    }
}
