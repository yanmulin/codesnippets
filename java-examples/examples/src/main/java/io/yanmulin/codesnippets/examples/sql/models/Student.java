package io.yanmulin.codesnippets.examples.sql.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    Long id;
    String name;
    Integer age;
}
