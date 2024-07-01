package io.yanmulin.codesnippets.spring.ioc;

import io.yanmulin.codesnippets.spring.ioc.circular.PrototypeServiceAImpl;
import io.yanmulin.codesnippets.spring.ioc.circular.SingletonServiceAImpl;
import io.yanmulin.codesnippets.spring.ioc.messages.SayHello;
import io.yanmulin.codesnippets.spring.ioc.movies.CSVMovieFinderImpl;
import io.yanmulin.codesnippets.spring.ioc.movies.MovieListerImpl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import java.util.Locale;

public class BeanFactoryExamples {

    private static final String MOVIE_FINDER_BEAN_NAME = "movieFinder";
    private static final String MOVIE_LISTER_BEAN_NAME = "movieLister";

    public BeanFactory registry() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition(MOVIE_FINDER_BEAN_NAME,
                BeanDefinitionBuilder.rootBeanDefinition(CSVMovieFinderImpl.class)
                        .addPropertyValue("filename", "/movies.csv")
                        .getBeanDefinition());
        beanFactory.registerBeanDefinition(MOVIE_LISTER_BEAN_NAME,
                BeanDefinitionBuilder.rootBeanDefinition(MovieListerImpl.class)
                        .addConstructorArgReference(MOVIE_FINDER_BEAN_NAME)
                        .getBeanDefinition());
        return beanFactory;
    }

    public BeanFactory xml() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions("/beans.xml");
        return beanFactory;
    }

    public BeanFactory autoScan() {
        return new ClassPathXmlApplicationContext("/auto-scanning.xml");
    }

    public BeanFactory placeholder() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("/placeholder.xml");
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setLocation(new ClassPathResource("/application.properties"));
        applicationContext.addBeanFactoryPostProcessor(configurer);
        applicationContext.refresh();
        return applicationContext;
    }

    public void getBean() {
//        BeanFactory beanFactory = placeholder();
        BeanFactory beanFactory = xml();
        MovieLister movieLister = beanFactory.getBean(MovieLister.class);
        System.out.println(movieLister.listAll());
    }

    public void circularSingleton() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("/circular.xml");
        SingletonServiceAImpl bean = beanFactory.getBean(SingletonServiceAImpl.class);
        System.out.println(bean);
    }

    public void circularPrototype() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("/circular.xml");
        PrototypeServiceAImpl bean = beanFactory.getBean(PrototypeServiceAImpl.class);
        System.out.println(bean);
    }

    public void messageSource() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("/messages.xml");
        SayHello sayHello = beanFactory.getBean(SayHello.class);
        sayHello.speak(Locale.CHINESE);
        sayHello.speak(Locale.ENGLISH);
    }

    public void loadMany() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext(new String[]{"beans.xml", "messages.xml", "circular.xml"});
        beanFactory.getBean(SayHello.class).speak(Locale.ENGLISH);
        MovieLister movieLister = beanFactory.getBean(MovieLister.class);
        System.out.println(movieLister.listAll());
    }

    public static void main(String[] args) {
//        new BeanFactoryExamples().getBean();
        new BeanFactoryExamples().circularSingleton();
//        new BeanFactoryExamples().circularPrototype();
//        new BeanFactoryExamples().messageSource();
//        new BeanFactoryExamples().loadMany();
    }
}