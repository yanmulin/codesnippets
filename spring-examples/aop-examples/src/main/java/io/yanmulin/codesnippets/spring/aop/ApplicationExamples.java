package io.yanmulin.codesnippets.spring.aop;

import io.yanmulin.codesnippets.spring.aop.target.IExecute;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FaultBarrierExamples {
    public void faultBarrier() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("/fault-barrier.xml");
        IExecute target = beanFactory.getBean("targetBean", IExecute.class);
        target.execute("hello world!", new RuntimeException("exception"));
    }

    public static void main(String[] args) {
        new FaultBarrierExamples().faultBarrier();
    }
}
