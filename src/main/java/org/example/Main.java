package org.example;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.tomcat.jdbc.pool.DataSource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** build.gradle
 * plugins {
 *     id 'application'
 * }
 * application {
 *     mainClass = 'org.example.Main'
 * }
 * repositories {
 *     mavenCentral()
 * }
 * dependencies {
 *     implementation 'org.mybatis:mybatis:3.4.6',
 *             'org.mybatis:mybatis-spring:2.0.7',
 *             'net.sourceforge.jtds:jtds:1.3.1',
 *             'org.apache.tomcat:tomcat-jdbc:7.0.88'
 * }
 *
 * Run: ./gradlew run &> out.log
 * you will see some queries giving 0 results.
 *
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting ... ");
        int nThreads = 100, nExecs = 10000;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nExecs; i++) {
            executorService.submit(Main::runQuery);
        }
        executorService.shutdown();
    }

    private static void runQuery() {
        try (SqlSession session = DbConnPoolFactory.createSessionFactory().openSession()) {
            long startTime = System.nanoTime();
            TestModel obj = session.getMapper(TestModelMapper.class).getOne();
            System.out.printf("Result length: %s | TT: %s \n", obj != null ? 1 : 0, (System.nanoTime() - startTime) / 1_000_000_000.0);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage().replace("\n", " "));
        }
    }

    private static class TestModel {
        String id;
        public void setId(String id) { this.id = id; }
    }

    private interface TestModelMapper {
        @Select("WAITFOR DELAY '00:00:05'; SELECT 1 id;") TestModel getOne();
    }

    private static class DbConnPoolFactory {
        public static SqlSessionFactory createSessionFactory() {
            int queryTimeoutSecs = 7;
            int removeAbandonedTimeoutSecs = 3;
            DataSource ds = new DataSource();
            ds.setDriverClassName("net.sourceforge.jtds.jdbc.Driver");
            String DB_HOST = "localhost:1433";
            String DB_NAME = "test";
            ds.setUrl("jdbc:jtds:sqlserver://" + DB_HOST + "/" + DB_NAME);
            ds.setUsername("sa");
            ds.setPassword("Pass@word");
            ds.setInitialSize(5);
            ds.setMaxActive(50);
            ds.setMinIdle(20);
            ds.setMaxIdle(30);
            ds.setRemoveAbandoned(true);
            ds.setRemoveAbandonedTimeout(removeAbandonedTimeoutSecs);
            ds.setTimeBetweenEvictionRunsMillis(1_000);
            ds.setLogAbandoned(false);
            Environment env = new Environment("development", new JdbcTransactionFactory(), ds);
            Configuration mybatisConf = new Configuration(env);
            mybatisConf.setDefaultStatementTimeout(queryTimeoutSecs);
            mybatisConf.addMapper(TestModelMapper.class);
            return new SqlSessionFactoryBuilder().build(mybatisConf);
        }
    }
}
