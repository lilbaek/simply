package com.lilbaek.simply.test.update;

import com.lilbaek.simply.DBClient;
import com.lilbaek.simply.exceptions.NoIdException;
import com.lilbaek.simply.test.domain.Post;
import com.lilbaek.simply.test.domain.PostIdEnabledClass;
import com.lilbaek.simply.test.domain.PostType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Sql(scripts = {"updatespecttest.sql"})
public class UpdateTest {
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
    public void updateRecordDirect() {
        final var id = "1";
        final var update = getUpdateRecord(id);
        client.update(update);
        comparePostRecord(update, getRecordFromDb(id));
    }

    @Test
    public void updateWithCondition() {
        final var id = "2";
        final var update = getUpdateRecord(id);
        client.update(update, new PostIdEnabledClass(id, true));
        comparePostRecord(update, getRecordFromDb(id));
    }

    @Test
    public void updateRecordSingleDirect() {
        final var id = "3";
        final var update = getUpdateRecord(id);
        client.updateSingle(update);
        comparePostRecord(update, getRecordFromDb(id));
    }

    @Test
    public void updateWithConditionError() {
        final var update = getUpdateRecord("1");
        final var exception = assertThrows(IllegalStateException.class, () -> client.updateSingle(update, new PostIdEnabledClass("9999", true)));
        assertTrue(exception.getMessage().contains("DBClient - Failed to update Post"));
    }

    @Test
    public void updateWithConditionAndRollbackError() {
        final var id = "4";
        final var update = getUpdateRecord(id);
        final var before = getRecordFromDb(id);
        final var transaction = new TransactionTemplate(transactionManager);
        final var exception = assertThrows(IllegalStateException.class, () -> transaction.execute(status -> {
            client.update(update);
            client.updateSingle(update, new PostIdEnabledClass("9999", true));
            return status;
        }));
        assertTrue(exception.getMessage().contains("DBClient - Failed to update Post"));
        comparePostRecord(before, getRecordFromDb(id));
    }

    @Test
    public void errorIfNoId() {
        assertThrows(NoIdException.class, () -> client.updateSingle(new DummyClass("1", "")));
    }

    @Test
    public void errorIfNoIdCondition() {
        assertThrows(NoIdException.class, () -> client.updateSingle(new DummyClass("1", ""), new DummyClass("1", "")));
    }

    @Test
    public void errorIfConditionIsSimpleType() {
        assertThrows(TypeMismatchDataAccessException.class, () -> client.updateSingle(getUpdateRecord("1"), 1));
    }

    private static Post getUpdateRecord(final String id) {
        return new Post(id,
                "Updated",
                false,
                LocalDate.of(2025, 1, 20),
                PostType.JOURNAL,
                BigDecimal.valueOf(2.5123456789),
                List.of());
    }

    private static void comparePostRecord(final Post o1, final Post o2) {
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
}
