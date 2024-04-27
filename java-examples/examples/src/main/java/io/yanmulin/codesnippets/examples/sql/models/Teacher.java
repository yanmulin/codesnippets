package io.yanmulin.codesnippets.examples.sql.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Teacher {
    Long id;
    String name;
    String school;
}
