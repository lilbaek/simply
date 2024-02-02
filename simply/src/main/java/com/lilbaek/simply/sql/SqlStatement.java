package com.lilbaek.simply.sql;

import java.util.List;

public record SqlStatement(String sql, List<?> values) {

}
