package io.yanmulin.codesnippets.examples.apache;

import com.mysql.cj.jdbc.MysqlDataSource;
import io.yanmulin.codesnippets.examples.sql.Constants;
import io.yanmulin.codesnippets.examples.sql.models.Student;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class SqlConnPoolExamples {
    public static class ConnectionFactory extends BasePooledObjectFactory<Connection> {

        Properties properties;
        DataSource dataSource;

        public ConnectionFactory() throws IOException {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream stream = classLoader.getResourceAsStream(Constants.JDBC_CONFIG_FILE_NAME);
            Properties properties = new Properties();
            properties.load(stream);
            this.properties = properties;

            String url = properties.getProperty(Constants.JDBC_URL_CONFIG_KEY);
            MysqlDataSource dataSource = new MysqlDataSource();
            dataSource.setUrl(url);
            this.dataSource = dataSource;
        }

        @Override
        public Connection create() throws Exception {
            System.out.println("creating a new connection");
            String username = properties.getProperty(Constants.JDBC_USERNAME_CONFIG_KEY);
            String password = properties.getProperty(Constants.JDBC_PASSWORD_CONFIG_KEY);
            return username != null ? dataSource.getConnection(username, password)
                    : dataSource.getConnection();
        }

        @Override
        public void destroyObject(PooledObject<Connection> p) throws SQLException {
            System.out.println("destroying a connection");
            p.getObject().close();
        }

        @Override
        public PooledObject<Connection> wrap(Connection connection) {
            return new DefaultPooledObject<>(connection);
        }
    }

    public void test() throws Exception {
        GenericObjectPool<Connection> connPool = new GenericObjectPool<>(new ConnectionFactory());
        Connection connection = connPool.borrowObject();

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
            connPool.returnObject(connection);
            connPool.close();
        }
    }

    public static void main(String[] args) throws Exception {
        new SqlConnPoolExamples().test();
    }
}
