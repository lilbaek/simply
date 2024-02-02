package com.lilbaek.simply.test.select;

import com.lilbaek.simply.DBClient;
import com.lilbaek.simply.test.domain.Post;
import com.lilbaek.simply.test.domain.PostIdEnabledClass;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Sql(scripts = {"selectspecttest.sql"})
public class SelectSpecTest {
    @Autowired
    private DBClient client;

    @Test
    public void findByIdPrimitive() {
        final var record = client.findById("1", Post.class);
        assertEquals("1", record.id());
    }

    @Test
    public void findByIdCondition() {
        final var record = client.findById(new PostIdEnabledClass("6", false), Post.class);
        assertEquals("6", record.id());
        assertFalse(record.enabled());
    }

    @Test
    public void findByIdOrNullPrimitive() {
        final var record = client.findByIdOrNull("NOT-EXISTING", Post.class);
        assertNull(record);
    }

    @Test
    public void findByIdOptionalPrimitive() {
        final var record = client.findByIdOptional("NOT-EXISTING", Post.class);
        assertTrue(record.isEmpty());
    }

}
