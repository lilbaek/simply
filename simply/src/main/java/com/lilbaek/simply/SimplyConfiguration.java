package com.lilbaek.simply;

import com.lilbaek.simply.sql.SchemaReplacer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

@Configuration
public class SimplyConfiguration {

    private final JdbcClient jdbcClient;
    private final String schema;

    public SimplyConfiguration(final JdbcClient jdbcClient, final @Value("${simply.datasource.name:}") String schema) {
        this.jdbcClient = jdbcClient;
        this.schema = schema;
    }

    @Bean
    public DBClient getDbClient() {
        return new DBClient(jdbcClient, schemaReplacer());
    }

    @Bean
    public SchemaReplacer schemaReplacer() {
        return new SchemaReplacer(schema);
    }
}
