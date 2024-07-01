package io.yanmulin.codesnippets.spring.aop.spring.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/index.html")
    public String index() {
        return "index";
    }

}
