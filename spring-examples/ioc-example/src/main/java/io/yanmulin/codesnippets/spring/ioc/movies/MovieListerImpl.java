package io.yanmulin.codesnippets.spring.ioc.movies;

import io.yanmulin.codesnippets.spring.ioc.Movie;
import io.yanmulin.codesnippets.spring.ioc.MovieFinder;
import io.yanmulin.codesnippets.spring.ioc.MovieLister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MovieListerImpl implements MovieLister {
    private final MovieFinder finder;

    @Autowired
    public MovieListerImpl(MovieFinder finder) {
        this.finder = finder;
    }

    @Override
    public List<Movie> listAll() {
        return finder.findAll();
    }
}
