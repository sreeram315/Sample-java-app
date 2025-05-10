package org.example.db;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.tomcat.jdbc.pool.DataSource;

import java.io.IOException;
import java.io.InputStream;

public class ConnectionPoolFactory {

    public static SqlSessionFactory createSessionFactory() throws IOException {
        DataSource ds = getDataSource();

        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);

        org.apache.ibatis.session.Configuration config = new SqlSessionFactoryBuilder().build(inputStream).getConfiguration();
        Environment env = new Environment("development", new JdbcTransactionFactory(), ds);
        config.setEnvironment(env);

        return new SqlSessionFactoryBuilder().build(config);
    }

    private static DataSource getDataSource() {
        DataSource ds = new DataSource();
        ds.setDriverClassName("net.sourceforge.jtds.jdbc.Driver");
        String DB_HOST = "localhost:1433";
        String DB_NAME = "test";
        ds.setUrl("jdbc:jtds:sqlserver://" + DB_HOST + "/" + DB_NAME);
        ds.setUsername("sa");
        ds.setPassword("Pass@word");

        // Pooling config
        ds.setInitialSize(5);
        ds.setMaxActive(50);
        ds.setMinIdle(20);
        ds.setMaxIdle(30);
        ds.setRemoveAbandoned(true);
        ds.setRemoveAbandonedTimeout(3);
        ds.setTimeBetweenEvictionRunsMillis(1_000);
        ds.setLogAbandoned(false);
        return ds;
    }

}
