package com.lilbaek.simply.sql;

public class SchemaReplacer {
    public static final String SCHEMA_REGEX = "\\{schema}";
    private final String schema;

    public SchemaReplacer(final String schema) {
        this.schema = schema;
    }

    public String replaceSchema(final String sql) {
        if (schema.isEmpty()) {
            return sql.replaceAll(SCHEMA_REGEX, "");
        }
        return sql.replaceAll(SCHEMA_REGEX, schema + ".");
    }
}
