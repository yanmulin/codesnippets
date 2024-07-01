package io.yanmulin.codesnippets.spring.aop.spring.ioc.circular;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SingletonServiceAImpl {
    @Autowired
    SingletonServiceBImpl b;

    public SingletonServiceAImpl() {}

    @PostConstruct
    public void initialize() {
        System.out.println("post construct " + getClass());
    }

}
