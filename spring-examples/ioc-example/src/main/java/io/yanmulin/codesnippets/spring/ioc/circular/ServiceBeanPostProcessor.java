package io.yanmulin.codesnippets.spring.ioc.circular;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

//@Component
public class ServiceBeanPostProcessor implements BeanPostProcessor {

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SingletonServiceAImpl) {
            return new SingletonServiceAImpl();
        }
        return bean;
    }
}
