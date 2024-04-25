package io.yanmulin.codesnippets.spring;

import io.yanmulin.codesnippets.spring.config.AopConfiguration;
import io.yanmulin.codesnippets.spring.services.AspectExampleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AspectTests {
    @Autowired
    AspectExampleService aspectExampleService;

    @Autowired
    AopConfiguration aopConfiguration;

    @Test
    public void testMethodBeforeAdvice() {
        aspectExampleService.run();
    }
}
