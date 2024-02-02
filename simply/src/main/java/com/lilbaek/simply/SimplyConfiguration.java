package com.lilbaek.simply;

import com.lilbaek.simply.sql.SchemaReplacer;
import com.lilbaek.simply.sql.StatementLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

@Configuration
public class SimplyConfiguration {

    private final JdbcClient jdbcClient;
    private final String schema;
    private final boolean showSql;

    public SimplyConfiguration(final JdbcClient jdbcClient, final @Value("${simply.datasource.name:}") String schema,
                               final @Value("${simply.show-sql:false}") boolean showSql) {
        this.jdbcClient = jdbcClient;
        this.schema = schema;
        this.showSql = showSql;
    }

    @Bean
    public DBClient simplyDbClient() {
        return new DBClient(jdbcClient, simplySchemaReplacer(), simplyStatementLogger());
    }

    @Bean
    public SchemaReplacer simplySchemaReplacer() {
        return new SchemaReplacer(schema);
    }

    @Bean
    public StatementLogger simplyStatementLogger() {
        return new StatementLogger(showSql);
    }
}
