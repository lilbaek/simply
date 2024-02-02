package com.lilbaek.simply.test.service;

import com.lilbaek.simply.DBClient;
import com.lilbaek.simply.test.model.Post;
import com.lilbaek.simply.test.model.PostId;
import com.lilbaek.simply.test.model.PostIdEnabledClass;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PostService {
    private final DBClient dbClient;

    public PostService(final DBClient dbClient) {
        this.dbClient = dbClient;
    }

    public Optional<Post> findById(final String id) {
        return dbClient.sql("SELECT id,title,slug,enabled,date,time_to_read,tags FROM post WHERE id = :id")
                .param("id", id)
                .optional(Post.class);
    }

    public void create(final Post post) {
        dbClient.insert(post);
    }

    public void update(final Post post, final String id) {
        dbClient.updateSingle(new Post(id, post.title(), post.slug(), post.enabled(), post.date(), post.timeToRead(), post.tags()));
    }

    public void delete(final String id) {
        dbClient.deleteSingle(Post.class, new PostId(id));
    }
}
