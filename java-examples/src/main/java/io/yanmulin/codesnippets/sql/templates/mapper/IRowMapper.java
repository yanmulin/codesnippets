package io.yanmulin.codesnippets.sql.templates.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface IRowMapper<T> {
    List<T> mapRow(ResultSet rowSet) throws SQLException;
}
