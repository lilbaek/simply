package com.lilbaek.simply.test.delete;

import com.lilbaek.simply.DBClient;
import com.lilbaek.simply.test.domain.Post;
import com.lilbaek.simply.test.domain.PostId;
import com.lilbaek.simply.test.domain.PostType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Sql(scripts = {"deletespecttest.sql"})
public class DeleteTest {
    private static final String SQL_SELECT = """
            SELECT id, title, enabled, date, type, stars
            FROM POST
            WHERE id = :id
            """;

    @Autowired
    private DBClient client;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    public void deleteRecord() {
        assertNotNull(getRecordFromDb("1"));
        client.delete(Post.class, new PostId("1"));
        assertNull(getRecordFromDb("1"));
    }

    @Test
    public void deleteRecordNoCondition() {
        final var recordFromDb = getRecordFromDb("1");
        assertNotNull(recordFromDb);
        client.delete(recordFromDb);
        assertNull(getRecordFromDb("1"));
    }

    @Test
    public void deleteSingleError() {
        final var exception = assertThrows(IllegalStateException.class, () -> {
            client.deleteSingle(Post.class, new PostId("9999"));
        });
        assertTrue(exception.getMessage().contains("DBClient - Delete count was not 1"));
    }

    @Test
    public void deleteWithError() {
        final var exception = assertThrows(IllegalStateException.class, () -> {
            client.deleteSingle(new Post("9999", "X", false, LocalDate.now(), PostType.ARTICLE, BigDecimal.ONE, List.of()));
        });
        assertTrue(exception.getMessage().contains("DBClient - Delete count was not 1"));
    }

    @Test
    public void deleteSingleWithTransaction() {
        final var transaction = new TransactionTemplate(transactionManager);
        final var exception = assertThrows(IllegalStateException.class, () -> {
            transaction.execute(status -> {
                client.deleteSingle(Post.class, new PostId("1"));
                client.deleteSingle(Post.class, new PostId("9999"));
                return status;
            });
        });
        assertTrue(exception.getMessage().contains("DBClient - Delete count was not 1"));
        assertNotNull(getRecordFromDb("1"));
    }

    private Post getRecordFromDb(final String id) {
        return client.sql(SQL_SELECT).param("id", id)
                .recordOrNull(Post.class);
    }
}
