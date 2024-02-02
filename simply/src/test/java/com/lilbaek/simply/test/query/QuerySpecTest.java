package com.lilbaek.simply.test.query;

import com.lilbaek.simply.DBClient;
import com.lilbaek.simply.test.domain.PostType;
import com.lilbaek.simply.test.query.testdata.PostRecordFromQuery;
import com.lilbaek.simply.test.query.testdata.PostRecordNoArgs;
import com.lilbaek.simply.test.query.testdata.PostRecordWithConversion;
import com.lilbaek.simply.test.query.testdata.PostRecordWithTransient;
import com.lilbaek.simply.test.query.testdata.PostRecordWithWrongParams;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Sql(scripts = {"queryspecttest.sql"})
public class QuerySpecTest {
    @Autowired
    private DBClient client;

    @Test
    public void getPostRecord() {
        final var record = client.sql("""
                        SELECT id
                        FROM POST
                        WHERE id = :id
                        """).param("id", "1")
                .record(PostRecordFromQuery.class);
        assertEquals("1", record.id());
    }

    @Test
    public void shouldReturnNullRecord() {
        final var record = client.sql("""
                        SELECT id
                        FROM POST
                        WHERE id = :id
                        """).param("id", "NOT-EXISTING")
                .recordOrNull(PostRecordFromQuery.class);
        assertNull(record);
    }

    @Test
    public void shouldReturnOptional() {
        final var record = client.sql("""
                        SELECT id
                        FROM POST
                        WHERE id = :id
                        """).param("id", "NOT-EXISTING")
                .optional(PostRecordFromQuery.class);
        assertTrue(record.isEmpty());
    }

    @Test
    public void shouldReturnOptionalWithValue() {
        final var record = client.sql("""
                        SELECT id
                        FROM POST
                        WHERE id = :id
                        """).param("id", "1")
                .optional(PostRecordFromQuery.class);
        assertFalse(record.isEmpty());
    }

    @Test
    void shouldReturnRecordTransient() {
        final var record = client.sql("""
                        SELECT id
                        FROM POST
                        WHERE id = :id
                        """).param("id", "1")
                .record(PostRecordWithTransient.class);
        assertEquals("1", record.id());
        assertNull(record.propNotInQuery());
        assertNull(record.otherPropNotInQuery());
    }

    @Test
    void shouldReturnSagRecordWithConversion() {
        final var record = client.sql("""
                        SELECT enabled, date, type, stars
                        FROM POST
                        WHERE id = :id
                        """).param("id", "1")
                .record(PostRecordWithConversion.class);
        assertEquals(true, record.enabled());
        assertEquals(LocalDate.of(2024, 2, 2), record.date());
        assertEquals(PostType.ARTICLE, record.type());
        assertEquals(0, BigDecimal.valueOf(8.876432).compareTo(record.stars()));
    }

    @Test
    void shouldReturnList() {
        final var record = client.sql("""
                        SELECT enabled, date, type, stars
                        FROM POST
                        """)
                .list(PostRecordWithConversion.class);
        assertEquals(6, record.size());
        assertEquals(PostType.ARTICLE, record.get(0).type());
        assertEquals(PostType.OTHER, record.get(1).type());
        assertEquals(PostType.JOURNAL, record.get(2).type());
        assertEquals(PostType.ARTICLE, record.get(3).type());
    }

    @Test
    void shouldReturnValue() {
        final var val = client.sql("""
                        SELECT time_to_read
                        FROM POST
                        WHERE id = :id
                        """).param("id", "6")
                .value(int.class);
        assertEquals(89, val);
    }

    @Test
    void shouldReturnValueNull() {
        final var val = client.sql("""
                        SELECT time_to_read
                        FROM POST
                        WHERE id = :id
                        """).param("id", "9999")
                .valueOrNull(Integer.class);
        assertNull(val);
    }

    @Test
    void shouldThrowErrorWhenNoResults() {
        assertThrows(EmptyResultDataAccessException.class, () -> client.sql("""
                       SELECT enabled, date, type, stars
                        FROM POST
                        WHERE id = :id
                        """).param("id", "9999")
                .record(PostRecordWithConversion.class));
    }

    @Test
    void shouldThrowErrorWhenTooManyResults() {
        assertThrows(IncorrectResultSizeDataAccessException.class, () -> client.sql("""
                        SELECT enabled, date, type, stars
                        FROM POST
                        """).param("id", "9999")
                .record(PostRecordWithConversion.class));
    }

    @Test
    void shouldThrowErrorWhenTooManyResultsNull() {
        assertThrows(IncorrectResultSizeDataAccessException.class, () -> client.sql("""
                        SELECT enabled, date, type, stars
                        FROM POST
                        """).param("id", "9999")
                .recordOrNull(PostRecordWithConversion.class));
    }

    @Test
    void shouldThrowErrorWhenValueNotSimpleNull() {
        assertThrows(TypeMismatchDataAccessException.class, () -> client.sql("""
                        SELECT time_to_read
                        FROM POST
                        WHERE id = :id
                        """).param("id", "1")
                .valueOrNull(PostRecordWithConversion.class));
    }

    @Test
    void shouldThrowErrorWhenValueNotSimple() {
        assertThrows(TypeMismatchDataAccessException.class, () -> client.sql("""
                        SELECT time_to_read
                        FROM POST
                        WHERE id = :id
                        """).param("id", "1")
                .value(PostRecordWithConversion.class));
    }

    @Test
    void shouldThrowExceptionOnMissingArgs() {
        final var exception = assertThrows(InvalidDataAccessApiUsageException.class, () -> client.sql("""
                        SELECT id
                        FROM POST
                        WHERE id = :id
                        """).param("id", "1")
                .record(PostRecordWithWrongParams.class));
        final var expectedMessage = "takes more arguments than returned";
        final var actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void shouldThrowExceptionOnMissingConstructorArgs() {
        final var exception = assertThrows(InvalidDataAccessApiUsageException.class, () -> client.sql("""
                        SELECT id
                        FROM POST
                        WHERE id = :id
                        """).param("id", "1")
                .record(PostRecordNoArgs.class));
        final var expectedMessage = "You need to have a minimum of 1 argument in your constructor";
        final var actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}
