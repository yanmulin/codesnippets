package io.yanmulin.codesnippets.spring.aop.spring.ioc.circular;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SingletonServiceBImpl {
    @Autowired
    SingletonServiceAImpl a;

    public SingletonServiceBImpl() {}
}
