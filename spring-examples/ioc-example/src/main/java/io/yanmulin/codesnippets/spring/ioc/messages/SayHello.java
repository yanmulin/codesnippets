package io.yanmulin.codesnippets.spring.aop.spring.ioc.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import java.util.Locale;

@Component
public class SayHello {
    @Autowired
    MessageSource messageSource;

    public void speak(Locale locale) {
        System.out.println(messageSource.getMessage("hello", new Object[]{}, locale));
    }
}
