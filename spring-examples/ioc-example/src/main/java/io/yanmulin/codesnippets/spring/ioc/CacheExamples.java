package io.yanmulin.codesnippets.spring.ioc;

import io.yanmulin.codesnippets.spring.ioc.cache.BookRepository;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CacheExamples {

    public void cache() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("cache.xml");
        BookRepository repository = beanFactory.getBean("bookRepository", BookRepository.class);
        System.out.println(repository.getByIsbn("isbn-1234"));
        System.out.println(repository.getByIsbn("isbn-5678"));
        System.out.println(repository.getByIsbn("isbn-1234"));
        repository.update("isbn-1234");
        System.out.println(repository.getByIsbn("isbn-1234"));
    }

    public static void main(String[] args) {
        new CacheExamples().cache();
    }
}
