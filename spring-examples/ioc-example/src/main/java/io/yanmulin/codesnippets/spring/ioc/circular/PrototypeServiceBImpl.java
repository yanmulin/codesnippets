package io.yanmulin.codesnippets.spring.ioc.circular;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrototypeServiceBImpl {
    @Autowired
    PrototypeServiceAImpl a;
}
