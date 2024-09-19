package com.lilbaek.simply.test.crudrepository;

import com.lilbaek.simply.DBClient;
import com.lilbaek.simply.SimpleCrudRepository;
import com.lilbaek.simply.test.domain.Post;
import org.springframework.stereotype.Repository;

@Repository
public class PostCrudRepository extends SimpleCrudRepository<Post, String> {
    protected PostCrudRepository(DBClient dbClient) {
        super(dbClient);
    }
}
