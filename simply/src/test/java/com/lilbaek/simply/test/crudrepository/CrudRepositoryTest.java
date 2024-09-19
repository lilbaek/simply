package com.lilbaek.simply.test.crudrepository;

import com.lilbaek.simply.test.domain.Post;
import com.lilbaek.simply.test.domain.PostType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Sql(scripts = {"repospecttest.sql"})
public class CrudRepositoryTest {
    @Autowired
    PostCrudRepository repo;

    @Test
    public void findById() {
        final var record = repo.findById("1").get();
        assertEquals("1", record.id());
        assertTrue(record.enabled());
    }

    @Test
    public void deleteById() {
        repo.findById("2");  // Check that it exist
        repo.deleteById("2"); // Delete it
        final var record = repo.findById("2"); // Check that it is gone
        assertFalse(record.isPresent());
        repo.findById("1"); // record one should be not be deleted
    }

    @Test
    public void update() {
        final var orgRecord = repo.findById("3").get();
        repo.update(new Post(orgRecord.id(), "Updated", orgRecord.enabled(), orgRecord.date(), orgRecord.type(), orgRecord.stars(), List.of()));
        final var updatedRecord = repo.findById("3").get();
        assertEquals("Updated", updatedRecord.title());
    }

    @Test
    public void insert() {
        repo.insert(new Post("9999", "Inserted", true, LocalDate.now(), PostType.ARTICLE, BigDecimal.ONE, List.of()));
        final var updatedRecord = repo.findById("9999").get();
        assertEquals("Inserted", updatedRecord.title());
    }
}
