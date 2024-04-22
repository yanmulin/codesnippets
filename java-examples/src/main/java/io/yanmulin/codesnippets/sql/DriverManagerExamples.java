package io.yanmulin.codesnippets.sql;

import com.mysql.cj.jdbc.MysqlDataSource;
import io.yanmulin.codesnippets.sql.models.Student;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DriverManagerExamples {
    public void test() throws IOException, SQLException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream stream = classLoader.getResourceAsStream(Constants.JDBC_CONFIG_FILE_NAME);

        Properties properties = new Properties();
        properties.load(stream);

        String url = properties.getProperty(Constants.JDBC_URL_CONFIG_KEY);
        String username = properties.getProperty(Constants.JDBC_USERNAME_CONFIG_KEY);
        String password = properties.getProperty(Constants.JDBC_PASSWORD_CONFIG_KEY);
        Connection connection = DriverManager.getConnection(url, username, password);
        try {
            CallableStatement statement = connection.prepareCall("SELECT * FROM student");
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Long id = rs.getLong(1);
                String name = rs.getString(2);
                Integer age = rs.getInt(3);
                System.out.println("row " + rs.getRow() + ": " + new Student(id, name, age));
            }
        } finally {
            connection.close();
        }
    }

    public static void main(String[] args) throws SQLException, IOException {
        new DriverManagerExamples().test();
    }
}
