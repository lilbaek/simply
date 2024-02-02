package com.lilbaek.simply.test.insert;

import com.lilbaek.simply.DBClient;
import com.lilbaek.simply.exceptions.NotAnEntityException;
import com.lilbaek.simply.test.domain.Post;
import com.lilbaek.simply.test.domain.PostType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Sql(scripts = {"insertspecttest.sql"})
public class InsertTest {
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
    public void insertRecord() {
        final var post = getPost();
        client.insert(post);
        comparePost(post, getRecordFromDb("1"));
    }

    @Test
    public void insertRecordWithException() {
        assertThrows(DuplicateKeyException.class, () -> {
            client.insert(getPost());
            client.insert(getPost());
        });
    }

    @Test
    public void insertRecordWithExceptionTransaction() {
        final var transaction = new TransactionTemplate(transactionManager);
        assertThrows(DuplicateKeyException.class, () -> transaction.execute(status -> {
            client.insert(getPost());
            client.insert(getPost());
            return status;
        }));
        assertNull(getRecordFromDbNull("1"));
    }

    @Test
    public void errorIfNotEntity() {
        assertThrows(NotAnEntityException.class, () -> client.insert(new DummyClass()));
    }

    private static Post getPost() {
        return new Post("1",
                "Record 1",
                true,
                LocalDate.of(2024, 1, 20),
                PostType.ARTICLE,
                BigDecimal.valueOf(2.4),
                List.of());
    }

    private void comparePost(final Post o1, final Post o2) {
        assertEquals(o1.id(), o2.id());
        assertEquals(o1.title(), o2.title());
        assertEquals(o1.enabled(), o2.enabled());
        assertEquals(o1.date(), o2.date());
        assertEquals(o1.type(), o2.type());
        assertEquals(0, o1.stars().compareTo(o2.stars()));
    }

    private Post getRecordFromDb(final String id) {
        return client.sql(SQL_SELECT).param("id", id)
                .record(Post.class);
    }

    private Post getRecordFromDbNull(final String id) {
        return client.sql(SQL_SELECT).param("id", id)
                .recordOrNull(Post.class);
    }
}
