package io.yanmulin.codesnippets.templates.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcUtils {

    private static final String JDBC_CONFIG_FILE_NAME = "jdbc.properties";
    private static final String JDBC_DRIVER_CLASS_CONFIG_KEY = "jdbc.driver.className";
    private static final String JDBC_URL_CONFIG_KEY = "jdbc.url";
    private static final String JDBC_USERNAME_CONFIG_KEY = "jdbc.username";
    private static final String JDBC_PASSWORD_CONFIG_KEY = "jdbc.password";

    private static Properties properties = new Properties();

    static {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream stream = classLoader.getResourceAsStream(JDBC_CONFIG_FILE_NAME);
            properties.load(stream);
            Class.forName(properties.getProperty(JDBC_DRIVER_CLASS_CONFIG_KEY));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty(JDBC_URL_CONFIG_KEY),
                properties.getProperty(JDBC_USERNAME_CONFIG_KEY),
                properties.getProperty(JDBC_PASSWORD_CONFIG_KEY)
        );
    }

    public static void closeConnection(ResultSet rs, Connection conn) throws SQLException {
        try {
            if (rs != null) {
                rs.close();
            }
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }


}
