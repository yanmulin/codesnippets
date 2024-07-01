package io.yanmulin.codesnippets.spring.data;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class TransactionalService {

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void readAndUpdate() {
        List<Integer> data = jdbcTemplate.query("SELECT data FROM tx_data WHERE id=2",
                (rs, rowNum) -> rs.getInt(1));
        Integer i = data.get(0);
        jdbcTemplate.update("UPDATE tx_data SET data=? WHERE id=2", i + 1);
    }
}
