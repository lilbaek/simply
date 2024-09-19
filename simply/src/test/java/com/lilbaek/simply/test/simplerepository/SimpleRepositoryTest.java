package com.lilbaek.simply.test.simplerepository;

import com.lilbaek.simply.test.BaseSqlTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Sql(scripts = {"repospecttest.sql"})
public class SimpleRepositoryTest extends BaseSqlTest {
    private SimplePostRepository underTest;

    @BeforeEach
    void setUp() {
        underTest = new SimplePostRepository(dbClient);
    }

    @Test
    public void findById() {
        final var record = underTest.findById("1").get();
        assertEquals("1", record.id());
        assertTrue(record.enabled());
    }
}
