package com.lilbaek.simply.database;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class DbClient {
    private final JdbcClient jdbcClient;

    public DbClient(final JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * Create an instance of <code>QuerySpec</code> for executing
     * a native SQL statement
     *
     * @param sqlString a native SQL query string
     * @return the new query instance
     */
    public QuerySpec sql(final String sqlString) {
        return new QuerySpecImpl(jdbcClient.sql(sqlString));
    }
}
