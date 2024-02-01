package com.lilbaek.simply.simply.sql;

import java.util.List;

public record SqlStatement(String sql, List<?> values) {

}
