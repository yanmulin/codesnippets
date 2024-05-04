package io.yanmulin.codesnippets.spring;

import io.yanmulin.codesnippets.services.AspectExampleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AspectTests {
    @Autowired
    AspectExampleService aspectExampleService;

    @Test
    public void testMethodBeforeAdvice() {
        aspectExampleService.run();
    }
}
