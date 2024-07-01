package io.yanmulin.codesnippets.spring.data;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.util.List;

public class TransactionExamples {
    private static final String URL = "jdbc:mysql://localhost:3306/test";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "pass";

    public void transactionManager() {
        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        TransactionDefinition txDef = new DefaultTransactionDefinition();
        TransactionStatus txStatus = transactionManager.getTransaction(txDef);

        List<Integer> data = jdbcTemplate.query("SELECT data FROM tx_data WHERE id=1",
                (rs, rowNum) -> rs.getInt(1));
        Integer i = data.get(0);
        jdbcTemplate.update("UPDATE tx_data SET data=? WHERE id=1", i + 1);

        transactionManager.rollback(txStatus);
    }

    private void nested() {
        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition();
        txDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        TransactionStatus txStatus = transactionManager.getTransaction(txDef);

        List<Integer> data = jdbcTemplate.query("SELECT data FROM tx_data WHERE id=2",
                (rs, rowNum) -> rs.getInt(1));
        Integer i = data.get(0);
        jdbcTemplate.update("UPDATE tx_data SET data=? WHERE id=2", i + 1);
        transactionManager.commit(txStatus);
    }

    public void propagation() {
        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        TransactionDefinition txDef = new DefaultTransactionDefinition();

        TransactionStatus txStatus = transactionManager.getTransaction(txDef);

        List<Integer> data = jdbcTemplate.query("SELECT data FROM tx_data WHERE id=1",
                (rs, rowNum) -> rs.getInt(1));
        Integer i = data.get(0);
        jdbcTemplate.update("UPDATE tx_data SET data=? WHERE id=1", i + 1);
        nested();

        transactionManager.rollback(txStatus);
    }

    public void savepoint() {
        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        TransactionDefinition txDef = new DefaultTransactionDefinition();

        TransactionStatus txStatus = transactionManager.getTransaction(txDef);

        jdbcTemplate.update("UPDATE tx_data SET data=2 WHERE id=1");
        Object savepoint = txStatus.createSavepoint();

        try {
            jdbcTemplate.update("UPDATE tx_data SET data=2 WHERE id=2");
            throw new RuntimeException();
        } catch (RuntimeException exception) {
            txStatus.rollbackToSavepoint(savepoint);
            jdbcTemplate.update("UPDATE tx_data SET data=2 WHERE id=3");
        }

        transactionManager.commit(txStatus);
    }

    public void transactional() {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("/transactional.xml");
        TransactionalService service = beanFactory.getBean(TransactionalService.class);
        service.readAndUpdate();
    }

    public static void main(String[] args) {
        new TransactionExamples().propagation();
    }
}
