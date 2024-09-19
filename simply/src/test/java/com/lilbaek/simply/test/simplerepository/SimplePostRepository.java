package com.lilbaek.simply.test.simplerepository;

import com.lilbaek.simply.DBClient;
import com.lilbaek.simply.SimpleRepository;
import com.lilbaek.simply.test.domain.Post;
import org.springframework.stereotype.Repository;

@Repository
public class SimplePostRepository extends SimpleRepository<Post, String> {
    protected SimplePostRepository(final DBClient dbClient) {
        super(dbClient);
    }
}
