package io.yanmulin.codesnippets.spring.ioc.cache;

import org.springframework.cache.annotation.Cacheable;

public class SimpleBookRepository {

    @Cacheable("books")
    public Book getByIsbn(String isbn) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return new Book(isbn, "some book");
    }

    public void update() {

    }
}
