package io.yanmulin.codesnippets.sql.templates;


import io.yanmulin.codesnippets.sql.templates.util.JdbcUtils;
import io.yanmulin.codesnippets.sql.templates.mapper.IRowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate {
    public <T> List<T> query(String sql, IRowMapper<T> rowMapper, Object... params) throws SQLException {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = JdbcUtils.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i = 1; i <= params.length; i++) {
                ps.setObject(i, params[i - 1]);
            }
            rs = ps.executeQuery();
            return rowMapper.mapRow(rs);
        } finally {
            JdbcUtils.closeConnection(rs, conn);
        }
    }

    public int update(String sql, Object... params) throws SQLException {
        Connection conn = null;
        try {
            conn = JdbcUtils.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i = 1; i <= params.length; i++) {
                ps.setObject(i, params[i - 1]);
            }
            return ps.executeUpdate();
        } finally {
            JdbcUtils.closeConnection(null, conn);
        }
    }
}
