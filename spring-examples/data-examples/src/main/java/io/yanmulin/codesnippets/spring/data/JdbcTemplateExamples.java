package io.yanmulin.codesnippets.spring.data;

import lombok.Data;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.MySQLMaxValueIncrementer;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JdbcTemplateExamples {
    private static final String URL = "jdbc:mysql://localhost:3306/test";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "pass";

    @Data
    private class Student {
        Long id;
        String name;
        Integer age;
    }

    private class StudentRowMapper implements RowMapper<Student> {
        @Override
        public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
            Student student = new Student();
            student.setId(rs.getLong(1));
            student.setName(rs.getString(2));
            student.setAge(rs.getInt(3));
            return student;
        }
    }

    public void selectExample() {
        List<Student> students;
        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        students = jdbcTemplate.query("SELECT * FROM student", new StudentRowMapper());
        System.out.println("select with RowMapper: " + students);

        int count;
        PreparedStatementCreatorFactory pscf;
        PreparedStatementCreator psc;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameter[] sqlParameters = {
                new SqlParameter(Types.BIGINT),
        };

        pscf = new PreparedStatementCreatorFactory(
                "SELECT * FROM student WHERE id = ?",
                List.of(sqlParameters)
        );
        psc = pscf.newPreparedStatementCreator(Collections.singletonList(1L));
        students = jdbcTemplate.query(psc, new StudentRowMapper());
        System.out.println("select with PreparedStatementCreator: " + students);

        pscf = new PreparedStatementCreatorFactory(
                "UPDATE student SET name='Mark' WHERE id= ?",
                List.of(sqlParameters)
        );
        psc = pscf.newPreparedStatementCreator(Collections.singletonList(1L));
        count = jdbcTemplate.update(psc);
        System.out.println("update with PreparedStatementCreator, count=" + count);

        pscf = new PreparedStatementCreatorFactory(
                "INSERT student (name, age) VALUES ('Lily', 21), ('John', 23);"
        );
        pscf.setReturnGeneratedKeys(true);
        psc = pscf.newPreparedStatementCreator(Collections.emptyList());
        count = jdbcTemplate.update(psc, keyHolder);
        System.out.println("insert with PreparedStatementCreator, count=" + count + ", keys=" + keyHolder.getKeyList());

        int[] counts = jdbcTemplate.batchUpdate(
                "INSERT student (name, age) VALUES (?, ?)",
                List.of(new Object[]{"Bob", 26}, new Object[]{"Mike", 21}, new Object[]{"Hank", 22})
        );
        System.out.println("batch insert with sql, counts=" + Arrays.stream(counts).boxed().collect(Collectors.toList()));
    }

    public void maxValueIncrementer() {
        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        DataFieldMaxValueIncrementer incrementer = new MySQLMaxValueIncrementer(dataSource, "sequence", "value");
        System.out.println(incrementer.nextIntValue());
        System.out.println(incrementer.nextIntValue());
        System.out.println(incrementer.nextIntValue());
    }

    public void blob() {
        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameter[] sqlParameters = {
                new SqlParameter(Types.BLOB),
        };
        PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(
                "INSERT blob_table (`blob`) VALUES (?)",
                Arrays.asList(sqlParameters)
        );
        pscf.setReturnGeneratedKeys(true);
        PreparedStatementCreator psc = pscf.newPreparedStatementCreator(
                Collections.singletonList(new byte[]{1, 2, 3, 4})
        );

        jdbcTemplate.update(psc, keyHolder);
        System.out.println("insert blob: " + keyHolder.getKeyList());
    }

    public static void main(String[] args) {
        new JdbcTemplateExamples().blob();
    }
}