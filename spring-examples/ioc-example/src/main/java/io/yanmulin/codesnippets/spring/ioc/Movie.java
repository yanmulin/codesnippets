package io.yanmulin.codesnippets.spring.ioc;

import lombok.Data;

@Data
public class Movie {
    String name;
    Integer year;
    String[] categories;
    String[] languages;
    String[] countries;
}
