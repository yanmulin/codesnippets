package io.yanmulin.codesnippets.spring.ioc.movies;

import io.yanmulin.codesnippets.spring.ioc.Movie;
import io.yanmulin.codesnippets.spring.ioc.MovieFinder;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class CSVMovieFinderImpl implements MovieFinder {
    private static final String CSV_DELIMITER = ",";
    private static final String LIST_ITEM_DELIMITER = "/";

    @Value("/movies.csv")
    private String filename;

    public CSVMovieFinderImpl() {

    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public List<Movie> findAll() {
        List<Movie> results = new ArrayList<>();
        InputStream stream = getClass().getResourceAsStream(filename);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] comps = line.split(CSV_DELIMITER);
                Movie movie = new Movie();
                movie.setName(comps[0]);
                movie.setYear(Integer.valueOf(comps[1]));
                movie.setCategories(comps[2].split(LIST_ITEM_DELIMITER));
                movie.setLanguages(comps[3].split(LIST_ITEM_DELIMITER));
                movie.setCountries(comps[4].split(LIST_ITEM_DELIMITER));
                results.add(movie);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }
}
