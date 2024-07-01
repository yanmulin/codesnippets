package io.yanmulin.codesnippets.spring.ioc.circular;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SingletonServiceBImpl {

    SingletonServiceAImpl a;

    public SingletonServiceBImpl() {}

    @Autowired
    public void setA(SingletonServiceAImpl a) {
        this.a = a;
    }
}
