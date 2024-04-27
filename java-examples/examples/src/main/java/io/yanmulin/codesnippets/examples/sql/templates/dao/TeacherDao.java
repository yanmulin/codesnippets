package io.yanmulin.codesnippets.examples.sql.templates.dao;

import io.yanmulin.codesnippets.examples.sql.models.Teacher;
import io.yanmulin.codesnippets.examples.sql.templates.JdbcTemplate;
import io.yanmulin.codesnippets.examples.sql.templates.mapper.IRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TeacherDao {
    private class TeacherRowMapper implements IRowMapper<Teacher> {
        @Override
        public List<Teacher> mapRow(ResultSet rs) throws SQLException {
            List<Teacher> teachers = new ArrayList<>();
            while (rs.next()) {
                Teacher teacher = new Teacher();
                teacher.setId(rs.getLong(1));
                teacher.setName(rs.getString(2));
                teacher.setSchool(rs.getString(3));
                teachers.add(teacher);
            }
            return teachers;
        }
    }

    JdbcTemplate jdbcTemplate = new JdbcTemplate();
    TeacherRowMapper rowMapper = new TeacherRowMapper();

    public List<Teacher> list() {
        String sql = "SELECT id,name,school FROM teacher";
        try {
            return jdbcTemplate.query(sql, rowMapper);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int save(Teacher teacher) {
        String sql = "INSERT INTO student (name,school) VALUES (?,?)";
        try {
            return jdbcTemplate.update(sql, teacher.getName(),teacher.getSchool());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String[] args) {
        TeacherDao dao = new TeacherDao();
        assert dao.list().size() == 0;
        assert dao.save(new Teacher(null, "Steve", "Computer Science")) == 1;
        assert dao.save(new Teacher(null, "John", "Biology")) == 1;
        assert dao.list().size() >= 2;
    }
}
