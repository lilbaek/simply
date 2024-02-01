package com.lilbaek.simply.service;

import com.lilbaek.simply.model.PostIdClass;
import com.lilbaek.simply.simply.DBClient;
import com.lilbaek.simply.model.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class PostService {
    private final DBClient dbClient;
    private final TransactionTemplate transactionTemplate;

    public PostService(DBClient dbClient, PlatformTransactionManager transactionManager) {
        this.dbClient = dbClient;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
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
        transactionTemplate.execute(status -> {
            dbClient.insert(post);
            update(post, post.id());
            return "";
        });
        try {
            transactionTemplate.execute(status -> {
                delete(post.id());
                return "";
            });
        } catch (Exception e) {
            System.out.println(e);
        }
        Optional<Post> byId = findById(post.id());
        System.out.println(byId);
    }

    public void update(final Post post, final String id) {
        dbClient.updateSingle(new Post(id, post.title(), post.slug(), post.enabled(), post.date(), post.timeToRead(), post.tags()));

        dbClient.updateSingle(new Post(id, post.title(), post.slug(), post.enabled(), post.date(), post.timeToRead(), post.tags()),
                        new PostIdClass(id, true));

    }

    public void delete(final String id) {
        dbClient.deleteSingle(Post.class, new PostIdClass(id, false));
        dbClient.deleteSingle(new Post(id, "", "", false, LocalDate.MAX, 1, ""));
    }
}
