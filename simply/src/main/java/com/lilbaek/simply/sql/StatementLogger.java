package com.lilbaek.simply.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class StatementLogger {
    private final boolean enabled;
    private static final Logger LOGGER = LoggerFactory.getLogger(StatementLogger.class);

    public StatementLogger(final boolean enabled) {
        this.enabled = enabled;
    }

    public void logStatement(final String sql, final List<?> values) {
        if (enabled) {
            LOGGER.info(sql + " | Values: " + String.join(",", values.stream().map(Object::toString).toList()));
        }
    }

    public void logStatement(final String sql, final Map<String, Object> parms) {
        if (enabled) {
            LOGGER.info(sql + " | Params: " + String.join(",", parms.entrySet().stream().map(x -> x.getKey() + " = " + x.getValue()).toList()));
        }
    }
}
