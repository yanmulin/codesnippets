package io.yanmulin.codesnippets.performance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class Controller {
    @Autowired
    PeopleService peopleService;

    @RequestMapping("/visit")
    public List<People> visit() {
        return peopleService.getPeople();
    }

    @RequestMapping("/run")
    public void run() {
        peopleService.compute();
    }
}
