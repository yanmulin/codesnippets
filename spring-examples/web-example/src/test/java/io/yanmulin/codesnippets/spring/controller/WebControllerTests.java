package io.yanmulin.codesnippets.spring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebControllerTests {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    public void testIndex() {
        String response = restTemplate.getForObject("/index.html", String.class);
        System.out.println(response);
    }
}
