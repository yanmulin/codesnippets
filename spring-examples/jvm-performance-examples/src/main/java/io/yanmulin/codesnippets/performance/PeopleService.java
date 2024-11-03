package io.yanmulin.codesnippets.performance;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PeopleService {

    Random random = new Random();

    public List<People> getPeople() {
        int latency = random.nextInt(10, 20);
        try {
            Thread.sleep(latency);
        } catch (InterruptedException e) {}

        byte[] bytes = new byte[1024 * 1024];
        List<byte[]> list = new ArrayList<>();
        list.add(bytes);

        return List.of(
                new People("John", 25),
                new People("Peter", 21),
                new People("Mary", 22),
                new People("Terry", 28),
                new People("Bill", 23)
        );
    }

    public void compute() {
        AtomicInteger i = new AtomicInteger();
        while (true) {
            i.incrementAndGet();
        }
    }
}
