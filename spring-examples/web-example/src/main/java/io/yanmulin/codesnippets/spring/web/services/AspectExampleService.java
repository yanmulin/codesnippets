package io.yanmulin.codesnippets.spring.aop.spring.web.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AspectExampleService {
    public void run() {
        log.info("method running");
    }
}
