package io.yanmulin.codesnippets.spring.aop;

import io.yanmulin.codesnippets.spring.aop.target.IExecute;
import io.yanmulin.codesnippets.spring.aop.target.IStatsTracker;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AopExamples {

    public void proxyFactoryBeanJdkProxy() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("/proxy-factory-bean-jdk-proxy.xml");
        IExecute target = beanFactory.getBean("targetProxy", IExecute.class);
        target.execute("hello world", null);
    }

    public void proxyFactoryBeanCGLibProxy() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("/proxy-factory-bean-cglib-proxy.xml");
        IExecute target = beanFactory.getBean("targetProxy", IExecute.class);
        target.execute("hello world", null);
    }

    public void autoProxyCreator() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("/auto-proxy-creator.xml");
        IExecute target = beanFactory.getBean("targetBean", IExecute.class);
        target.execute("hello world", null);
    }

    public void aspect() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("/aspect.xml");
        IExecute target = beanFactory.getBean("targetBean", IExecute.class);
        target.execute("hello world!", null);

        System.out.println("---");
        target.execute("hello world!", new RuntimeException("exception"));
    }

    public void aspectOrder() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("/aspect-order.xml");
        IExecute target = beanFactory.getBean("targetBean", IExecute.class);
        target.execute("hello world!", null);
    }

    public void introduction() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("/introduction.xml");
        IStatsTracker target = beanFactory.getBean("targetBean", IStatsTracker.class);
        target.increment();
    }

    public static void main(String[] args) {
//        new AopExamples().proxyFactoryBeanCGLibProxy();
//        new AopExamples().autoProxyCreator();
        new AopExamples().aspect();
//        new AopExamples().aspectOrder();
//        new AopExamples().introduction();
    }

}