package io.yanmulin.codesnippets.sql;

import com.mysql.cj.jdbc.MysqlDataSource;
import io.yanmulin.codesnippets.sql.models.Student;

import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DataSourceExamples {
    public void test() throws IOException, SQLException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream stream = classLoader.getResourceAsStream(Constants.JDBC_CONFIG_FILE_NAME);

        Properties properties = new Properties();
        properties.load(stream);

        String url = properties.getProperty(Constants.JDBC_URL_CONFIG_KEY);
        String username = properties.getProperty(Constants.JDBC_USERNAME_CONFIG_KEY);
        String password = properties.getProperty(Constants.JDBC_PASSWORD_CONFIG_KEY);
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl(url);
        Connection connection = username == null ? dataSource.getConnection() :
                dataSource.getConnection(username, password);
        CallableStatement statement = connection.prepareCall("SELECT * FROM student");
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            Long id = rs.getLong(1);
            String name = rs.getString(2);
            Integer age = rs.getInt(3);
            System.out.println("row " + rs.getRow() + ": " + new Student(id, name, age));
        }
    }

    public static void main(String[] args) throws SQLException, IOException {
        new DataSourceExamples().test();
    }
}
