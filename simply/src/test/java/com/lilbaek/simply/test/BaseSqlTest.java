package com.lilbaek.simply.test;

import com.lilbaek.simply.DBClient;
import com.lilbaek.simply.Logger;
import com.lilbaek.simply.sql.SchemaReplacer;
import org.hsqldb.jdbc.JDBCDriver;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.sql.DataSource;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
@ContextConfiguration(classes = BaseSqlTest.DBClientTestConfiguration.class)
public abstract class BaseSqlTest {
    @Autowired
    protected DBClient dbClient;

    public static class DBClientTestConfiguration {
        public DBClientTestConfiguration() {
        }

        @Bean
        DataSource testDataSource() {
            return new SimpleDriverDataSource(new JDBCDriver(), "jdbc:hsqldb:mem:testdb;DB_CLOSE_DELAY=-1", "SA", "");
        }

        @Bean
        DBClient testDbClient() {
            return new DBClient(JdbcClient.create(testDataSource()), new SchemaReplacer(""), new Logger(true));
        }
    }
}
