package io.yanmulin.codesnippets.spring.aop;

import io.yanmulin.codesnippets.spring.aop.target.DataSource;
import io.yanmulin.codesnippets.spring.aop.target.IExecute;
import io.yanmulin.codesnippets.spring.aop.target.NestableInvocationTarget;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class ApplicationExamples {
    public void faultBarrier() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("/fault-barrier.xml");
        IExecute target = beanFactory.getBean("targetBean", IExecute.class);
        target.execute("hello world!", new RuntimeException("exception"));
    }

    public void executeTimeRecorder() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("/execute-time.xml");
        IExecute target = beanFactory.getBean("targetBean", IExecute.class);
        target.execute("hello world!", null);
    }

    private String read(DataSource dataSource) {
        try {
            return dataSource.read();
        } catch (IOException exception) {
            System.out.println(exception);
        }
        return null;
    }

    public void hotSwappableDataSource() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("/hot-swap-data-sources.xml");
        DataSource dataSource = beanFactory.getBean("dataSource", DataSource.class);

        System.out.println(read(dataSource));
        System.out.println(read(dataSource));
    }

    public void nestableInvocation() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("/nestable-invocation.xml");
        NestableInvocationTarget target = beanFactory.getBean("targetBean", NestableInvocationTarget.class);
        target.execute("hello", null);
        System.out.println("---");
        target.setFromProxy(true);
        target.execute("hello", null);
    }

    public static void main(String[] args) {
        new ApplicationExamples().nestableInvocation();
    }
}
