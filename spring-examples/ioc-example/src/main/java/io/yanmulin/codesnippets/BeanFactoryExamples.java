package io.yanmulin.codesnippets;

import io.yanmulin.codesnippets.impl.CSVMovieFinderImpl;
import io.yanmulin.codesnippets.impl.MovieListerImpl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

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

    public static void main(String[] args) {
        BeanFactory beanFactory = new BeanFactoryExamples().placeholder();
        MovieLister movieLister = beanFactory.getBean(MovieLister.class);
        System.out.println(movieLister.listAll());
    }
}