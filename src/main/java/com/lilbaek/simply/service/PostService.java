package com.lilbaek.simply.service;

import com.lilbaek.simply.database.DbClient;
import com.lilbaek.simply.model.Post;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    private final DbClient dbClient;

    public PostService(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    public Optional<Post> findById(final String id) {
        final var title = dbClient.sql("SELECT title FROM post WHERE id = :id")
                        .param("id", id)
                        .value(String.class);

        return dbClient.sql("SELECT id,title,slug,enabled,date,time_to_read,tags FROM post WHERE id = :id")
                        .param("id", id)
                        .optional(Post.class);
    }

    public void create(final Post post) {
        int update = dbClient.sql("INSERT INTO post(id,title,slug,enabled,date,time_to_read,tags) values(?,?,?,?,?,?,?)")
                        .statementSpec()
                        .params(List.of(post.id(), post.title(), post.slug(), post.enabled() ? "Y": "N", post.date(), post.timeToRead(), post.tags()))
                        .update();
        Assert.state(update == 1, "Failed to create post " + post.title());
    }

    public void update(final Post post, final String id) {
        var updated = dbClient.sql("update post set title = ?, slug = ?, enabled = ?, date = ?, time_to_read = ?, tags = ? where id = ?")
                        .statementSpec()
                        .params(List.of(post.title(), post.slug(), post.enabled() ? "Y": "N", post.date(), post.timeToRead(), post.tags(), id))
                        .update();

        Assert.state(updated == 1, "Failed to update post " + post.title());
    }

    public void delete(final String id) {
        var updated = dbClient.sql("delete from post where id = :id")
                        .statementSpec()
                        .param("id", id)
                        .update();
        Assert.state(updated == 1, "Failed to delete post " + id);
    }
}
