package io.yanmulin.codesnippets.impl;

import io.yanmulin.codesnippets.Movie;
import io.yanmulin.codesnippets.MovieFinder;
import io.yanmulin.codesnippets.MovieLister;
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
