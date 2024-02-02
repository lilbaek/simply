package com.lilbaek.simply.sql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SchemaReplacer {
    private final String schema;

    public SchemaReplacer(final @Value("${simply.datasource.name:}") String schema) {
        this.schema = schema;
    }

    public String replaceSchema(final String sql) {
        return sql.replaceAll("\\{schema}", schema);
    }
}
