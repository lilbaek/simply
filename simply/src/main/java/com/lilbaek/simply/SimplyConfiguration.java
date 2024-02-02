package com.lilbaek.simply;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

@Configuration
public class SimplyConfiguration {

    private final JdbcClient jdbcClient;

    public SimplyConfiguration(final JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Bean
    public DBClient getDbClient() {
        return new DBClient(jdbcClient);
    }
}
