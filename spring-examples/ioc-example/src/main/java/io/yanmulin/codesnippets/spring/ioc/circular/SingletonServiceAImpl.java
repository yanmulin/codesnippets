package io.yanmulin.codesnippets.spring.ioc.circular;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SingletonServiceAImpl {
    SingletonServiceBImpl b;

    public SingletonServiceAImpl() {}

    @Autowired
    public void setB(SingletonServiceBImpl b) {
        this.b = b;
    }

    @PostConstruct
    public void initialize() {
        System.out.println("post construct " + getClass());
    }

}
