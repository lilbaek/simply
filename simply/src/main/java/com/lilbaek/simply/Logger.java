package com.lilbaek.simply;

import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class Logger {
    private final boolean enabled;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Logger.class);

    public Logger(final boolean enabled) {
        this.enabled = enabled;
    }

    public void logStatement(final String sql, final List<?> values) {
        if (enabled) {
            LOGGER.info(sql + " | Values: " + String.join(",", values.stream().map(Logger::getValue).toList()));
        }
    }

    private static String getValue(final Object obj) {
        return obj != null ? obj.toString() : "null";
    }

    public void logStatement(final String sql, final Map<String, Object> parms) {
        if (enabled) {
            LOGGER.info(sql + " | Params: " + String.join(",", parms.entrySet().stream().map(x -> x.getKey() + " = " + getValue(x)).toList()));
        }
    }
}
