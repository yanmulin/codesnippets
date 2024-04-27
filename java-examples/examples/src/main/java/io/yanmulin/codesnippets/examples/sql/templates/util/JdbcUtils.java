package io.yanmulin.codesnippets.examples.sql.templates.util;

import io.yanmulin.codesnippets.examples.sql.Constants;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcUtils {

    private static Properties properties = new Properties();

    static {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream stream = classLoader.getResourceAsStream(Constants.JDBC_CONFIG_FILE_NAME);
            properties.load(stream);
            Class.forName(properties.getProperty(Constants.JDBC_DRIVER_CLASS_CONFIG_KEY));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty(Constants.JDBC_URL_CONFIG_KEY),
                properties.getProperty(Constants.JDBC_USERNAME_CONFIG_KEY),
                properties.getProperty(Constants.JDBC_PASSWORD_CONFIG_KEY)
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
