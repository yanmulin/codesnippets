package io.yanmulin.codesnippets.spring.ioc.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

public class BookRepository {

    @Cacheable("books")
    public Book getByIsbn(String isbn) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return new Book(isbn, "some book");
    }

    @CacheEvict("books")
    public void update(String isbn) {
        System.out.println("updated book " + isbn);
    }
}
