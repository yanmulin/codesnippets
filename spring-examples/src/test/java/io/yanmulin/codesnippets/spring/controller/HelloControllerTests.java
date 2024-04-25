package io.yanmulin.codesnippets.spring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloControllerTests {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    public void testHello() {
        String response = restTemplate.getForObject("/hello", String.class);
        System.out.println(response);
    }
}
