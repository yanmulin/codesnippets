package io.yanmulin.codesnippets.spring.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RateLimitTests {

    String baseUri;

    WebTestClient webClient;

    @LocalServerPort
    int port = 0;

    @BeforeEach
    public void setUp() {
        baseUri = "http://localhost:" + port;
        webClient = WebTestClient.bindToServer()
                .responseTimeout(Duration.ofSeconds(10)).baseUrl(baseUri).build();
    }

    @Test
    public void testRateLimit() {
        for (int i=0;i<10;i++) {
            webClient.get().uri("/ratelimit").exchange()
                    .expectStatus().isOk();
        }
        webClient.get().uri("/ratelimit").exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
