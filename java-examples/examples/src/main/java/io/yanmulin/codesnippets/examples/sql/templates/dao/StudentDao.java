package io.yanmulin.codesnippets.examples.sql.templates.dao;

import io.yanmulin.codesnippets.examples.sql.models.Student;
import io.yanmulin.codesnippets.examples.sql.templates.JdbcTemplate;
import io.yanmulin.codesnippets.examples.sql.templates.mapper.IRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDao {

    private class StudentRowMapper implements IRowMapper<Student> {
        @Override
        public List<Student> mapRow(ResultSet rs) throws SQLException {
            List<Student> students = new ArrayList<>();
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getLong(1));
                student.setName(rs.getString(2));
                student.setAge(rs.getInt(3));
                students.add(student);
            }
            return students;
        }
    }

    JdbcTemplate jdbcTemplate = new JdbcTemplate();
    StudentRowMapper rowMapper = new StudentRowMapper();

    public List<Student> list() {
        String sql = "SELECT id,name,age FROM student";
        try {
            return jdbcTemplate.query(sql, rowMapper);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Student select(Long id) {
        String sql = "SELECT id,name,age FROM student WHERE id=?";
        try {
            List<Student> result = jdbcTemplate.query(sql, rowMapper, id);
            if (!result.isEmpty()) {
                return result.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int save(Student student) {
        String sql = "INSERT INTO student (name,age) VALUES (?,?)";
        try {
            return jdbcTemplate.update(sql, student.getName(),student.getAge());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int update(Student student) {
        String sql = "UPDATE student SET name=?,age=? WHERE id=?";
        try {
            return jdbcTemplate.update(sql, student.getName(), student.getAge(), student.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int delete(Student student) {
        String sql = "DELETE student WHERE id=?";
        try {
            return jdbcTemplate.update(sql, student.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String[] args) {
        StudentDao dao = new StudentDao();
        assert dao.list().size() == 0;
        assert dao.save(new Student(null, "Steve", 21)) == 1;
        assert dao.save(new Student(null, "John", 25)) == 1;
        assert dao.save(new Student(null, "Kay", 23)) == 1;
        assert dao.list().size() == 3;
        assert dao.update(new Student(1L, "Johny", 24)) == 1;
        assert dao.select(1L).equals(new Student(1L, "Johny", 24));
        assert dao.delete(new Student(1L, null, null)) == 1;
        assert dao.list().size() == 2;
    }
}
